/*
 * Copyright 2018 Michael Hahn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.trade.core.data.transformation.camel.processors;

import io.swagger.hdtapps.client.jersey.ApiClient;
import io.swagger.hdtapps.client.jersey.api.ApplicationsApi;
import io.swagger.hdtapps.client.jersey.api.TasksApi;
import io.swagger.hdtapps.client.jersey.api.TransformationsApi;
import io.swagger.hdtapps.client.jersey.model.*;
import io.swagger.trade.client.jersey.api.DataElementInstanceApi;
import io.swagger.trade.client.jersey.api.DataObjectInstanceApi;
import io.swagger.trade.client.jersey.api.DataValueApi;
import io.swagger.trade.client.jersey.model.*;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.support.ServiceSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.auditing.events.DataChangeEvent;
import org.trade.core.model.ABaseResource;
import org.trade.core.model.data.DataElement;
import org.trade.core.model.data.DataModel;
import org.trade.core.model.data.DataObject;
import org.trade.core.model.data.DataValue;
import org.trade.core.model.data.instance.DataElementInstance;
import org.trade.core.model.data.instance.DataObjectInstance;
import org.trade.core.model.dataTransformation.DataTransformation;
import org.trade.core.query.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class provides a custom processor for data transformations which triggers the selected data
 * transformation logic with the properties and data specified in the underlying data transformation.
 * <p>
 * Created by hahnml on 25.01.2018
 */
public class CamelDataTransformationProcessor extends ServiceSupport implements Processor {

    private Logger logger = LoggerFactory.getLogger("org.trade.core.data.transformation.camel.processors.CamelDataTransformationProcessor");

    private static final String INTERNAL = "TraDE Middleware";
    private static final String DATA_URL_SUFFIX = "/data";

    private ApiClient transformationClient;
    private TasksApi transformationTaskApi;
    private TransformationsApi transformationApi;
    private ApplicationsApi appApi;

    private DataTransformation transformation;

    private io.swagger.trade.client.jersey.ApiClient tradeClient;
    private DataValueApi dataValueApi;
    private DataObjectInstanceApi dataObjectInstanceApi;
    private DataElementInstanceApi dataElementInstanceApi;

    public CamelDataTransformationProcessor(ApiClient transformationClient, io.swagger.trade.client.jersey.ApiClient
            tradeClient, DataTransformation transformation) {
        this.transformationClient = transformationClient;
        this.tradeClient = tradeClient;
        this.transformation = transformation;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        DataChangeEvent event = (DataChangeEvent) exchange.getIn().getBody();

        // TODO: Introduce activation/trigger conditions for data transformations which allow to specify
        // conditions that need to be fulfilled in order to actually invoke specified transformation logic,
        // e.g., if a collection data element should be not transformed before all its entries are available.
        // Therefore, the introduced query syntax should be extended and reused.

        // We have to potentially trigger the transformation multiple times, if the data value on which the
        // DataChangeEvent was based is associated to more than one data element instance since the resulting values
        // when evaluating the specified parameter queries might be different
        if (transformation.getRelatedDataElementInstances().size() >= 1) {
            // Get the cached data element instances (context) based on the event identifier (ID of changed data value)
            for (DataElementInstance elementInstance : transformation.getRelatedDataElementInstances().get(event
                    .getIdentifier())) {
                if (this.transformation.getHdtTransformer() != null) {
                    // Call the transformation while using the provided element instance as a basis for resolution of
                    // resources/objects, e.g., in queries
                    callTransformationLogic(elementInstance);
                } else {
                    String msg = "Unable to resolve a transformation app " +
                            "for the given QName '" + this.transformation.getTransformerQName() + "'. Please check if" +
                            " the QName is valid and retry again.";
                    exchange.setException(new Exception(msg));

                    logger.error(msg);
                }
            }
        } else {
            String msg = "The context to invoke the " +
                    "transformation with name '" + this.transformation.getTransformerQName() + "' could not be " +
                    "resolved.";
            exchange.setException(new Exception(msg));

            logger.error(msg);
        }
    }

    private void callTransformationLogic(DataElementInstance instanceContext) throws Exception {
        Transformation transf = (Transformation) this.transformation.getHdtTransformer();

        TransformationRequest request = new TransformationRequest();

        // Set the resolved app ID
        request.setAppID(transf.getAppID());

        // Set the resolved transformer ID
        request.setTransformationID(transf.getTransformationID());

        // Set the transformation provider
        request.setProviders(resolveProviders(transf));

        // Resolve data transformation parameters to know the input parameter values in the context of the
        // instance the data event was fired for (data object/element instances with same correlationProperties)
        if (transf.getInputParams() != null && !transf.getInputParams().isEmpty()) {
            request.setInputParams(resolveInputParameters(transf, instanceContext));
        }

        // Resolve data transformation source and target to know the inputs and outputs for transformation

        // Map simple (single value) data elements to file inputs
        if (transf.getInputFiles() != null && transf.getInputFiles().size() > 0) {
            request.setInputFiles(resolveInputFiles(transf, instanceContext));
        }

        // Map collection data elements to input file set
        if (transf.getInputFileSets() != null && transf.getInputFileSets().size() > 0) {
            request.setInputFileSets(resolveInputFileSets(transf, instanceContext));
        }

        // Resolve the target URL of the data value to store the result. This is resolved by identifing the
        // target data element of the data connector and then resolving the corresponding data element instance
        // and finally its related data value
        // TODO: Support collection data elements as targets (enable a collection of result endpoints) which
        // requires some kind of mapping concept
        String resultEndpoint = resolveAndPrepareResultEndpoint(transf, instanceContext);
        request.setResultsEndpoint(resultEndpoint);

        // Only trigger the transformation, if all required data could be resolved
        if (resultEndpoint != null) {
            transformationTaskApi.hdtappsApiCreateTask(request);
        }
    }

    private List<Provider> resolveProviders(Transformation transformation) {
        List<Provider> providers = new ArrayList<>();

        // By default we add all available providers
        providers.addAll(transformation.getProviders());

        return providers;
    }

    private List<RequestInputParameter> resolveInputParameters(Transformation transformation, DataElementInstance
            instanceContext) {
        List<RequestInputParameter> inputParams = new ArrayList<>();

        if (transformation.getInputParams() != null && !transformation.getInputParams().isEmpty()) {
            for (InputParameter param : transformation.getInputParams()) {
                // Map the provided parameters to the one required by the transformation
                if (this.transformation.getTransformerParameters().containsKey(param.getInputName())) {
                    // Translate the parameter to an input parameter
                    RequestInputParameter inp = new RequestInputParameter();
                    inp.setKey(param.getInputName());
                    inp.setParamType(param.getType());

                    String value = this.transformation.getTransformerParameters().get(param.getInputName());
                    if (value.startsWith(Query.QUERY_PREFIX)) {
                        inp.setValue(resolveQuery(Query.parseQuery(value), instanceContext));
                    } else {
                        inp.setValue(value);
                    }

                    inputParams.add(inp);
                }
            }
        }

        return inputParams;
    }

    /*
     * This method resolves the underlying query in the "context" for which the event was thrown
     */
    private String resolveQuery(Query valueQuery, DataElementInstance instanceContext) {
        String result = "";

        if (valueQuery.isValid()) {
            DataModel model = this.transformation.getDataModel();

            if (model != null) {
                // Try to resolve the data object
                DataObject dataObject = model.getDataObject(valueQuery.getDataObjectName());

                if (dataObject != null) {
                    // Queries like: "$dataObject..."

                    // Try to resolve the data element
                    DataElement dataElement = dataObject.getDataElement(valueQuery.getDataElementName());
                    if (dataElement != null) {
                        // Queries like: "$dataObject/dataElement..."

                        // Resolve the related data element instance based on the given context (correlation properties)
                        DataElementInstance elementInstance = dataElement.getDataElementInstanceByCorrelation
                                (instanceContext.getCorrelationProperties());

                        if (elementInstance != null) {
                            if (valueQuery.specifiesDataValue()) {
                                // Queries like: "$dataObject/dataElement/value..."

                                if (valueQuery.specifiesDataValueIndex()) {
                                    // Queries like: "$dataObject/dataElement/value[index]..."

                                    if (dataElement.isCollectionElement()) {
                                        String indexString = valueQuery.getIndexOfDataValue();
                                        int index = -1;

                                        // Get the associated data values
                                        List<DataValue> dataValues = elementInstance.getDataValues();

                                        if (dataValues != null && !dataValues.isEmpty()) {
                                            try {
                                                // Try to parse the string to an integer and subtract one (index in queries start
                                                // from 1)
                                                index = Integer.valueOf(indexString) - 1;
                                            } catch (NumberFormatException e) {
                                                // Check if the index is specified using one of the static index values
                                                if (indexString.equals(Query.INDEX.FIRST.name().toLowerCase())) {
                                                    index = 0;
                                                } else if (indexString.equals(Query.INDEX.LAST.name().toLowerCase())) {
                                                    index = dataValues.size() - 1;
                                                } else {
                                                    logger.error("The transformation with name '{}' specifies a query statement " +
                                                            "({}) with an index string '{}' other than 'first' or 'last'. " +
                                                            "Therefore, the empty string is used as" +
                                                            " query result value which might lead to undesired effects. Please " +
                                                            "correct the " +
                                                            "respective query statement in the underlying data dependency graph " +
                                                            "model.", this.transformation.getTransformerQName(), valueQuery
                                                            .getQueryString(), indexString);
                                                }
                                            }
                                            if (index >= 0 && index < dataValues.size()) {
                                                // Retrieve the value of the data value at the given index
                                                DataValue value = dataValues.get(index);

                                                if (valueQuery.specifiesPropertySelection()) {
                                                    // Queries like: "$dataObject/dataElement/value[index]?property"

                                                    switch (valueQuery.getProperty()) {
                                                        case URL:
                                                            io.swagger.trade.client.jersey.model.DataValueWithLinks
                                                                    valueWithLinks = null;
                                                            try {
                                                                valueWithLinks = this.dataValueApi.getDataValueDirectly(value
                                                                        .getIdentifier());
                                                            } catch (io.swagger.trade.client.jersey.ApiException e) {
                                                                logger.error("Resolution of the query statement '" + valueQuery
                                                                        .getQueryString() + "' for transformation '" + this
                                                                        .transformation.getTransformerQName() +
                                                                        "' caused an exception. Therefore, the empty string is used as" +
                                                                        " query result value which might lead to undesired effects.", e);
                                                            }

                                                            if (valueWithLinks != null && valueWithLinks.getDataValue() != null) {

                                                                // Retrieve the URL of the data value and use it as the query
                                                                // result
                                                                result = valueWithLinks.getDataValue().getHref();
                                                            }

                                                            break;
                                                        case SIZE:
                                                            // The size is by default 1
                                                            result = "1";
                                                            break;
                                                        default:
                                                            break;
                                                    }
                                                } else {
                                                    // Queries like: "$dataObject/dataElement/value[index]"
                                                    try {
                                                        // Retrieve the data of the data value and use it as the query result
                                                        result = new String(value.getData());
                                                    } catch (Exception e) {
                                                        logger.error("Resolution of the query statement '" + valueQuery
                                                                .getQueryString() + "' for transformation '" + this
                                                                .transformation.getTransformerQName() +
                                                                "' caused an exception. Therefore, the empty string is used as" +
                                                                " query result value which might lead to undesired effects.", e);
                                                    }
                                                }
                                            } else {
                                                logger.error("The transformation with name '{}' " +
                                                        "specifies a query statement ({}) does point to a data value with an " +
                                                        "index '{}' that is out of bounds. Therefore, the empty string is used as" +
                                                        " query result value which might lead to undesired effects. Please correct the " +
                                                        "respective query statement in the underlying data dependency graph " +
                                                        "model.", this.transformation.getTransformerQName(), valueQuery
                                                        .getQueryString(), indexString);
                                            }
                                        } else {
                                            logger.error("The transformation with name '{}' " +
                                                            "specifies a query statement ({}) that could not be resolved to " +
                                                            "an actual value for the given instance context with correlation " +
                                                            "properties '{}'. Therefore, the empty string is used as" +
                                                            " query result value which might lead to undesired effects.",
                                                    this.transformation.getTransformerQName(), valueQuery
                                                            .getQueryString(), instanceContext.getCorrelationProperties());
                                        }
                                    } else {
                                        logger.error("The transformation with name '{}' " +
                                                "specifies a query statement ({}) that contains an index " +
                                                "selection but the underlying data element is not representing a " +
                                                "collection of values. Therefore, the empty string is used as" +
                                                " query result value which might lead to undesired effects. Please correct the " +
                                                "respective query statement in the underlying data dependency graph " +
                                                "model.", this.transformation.getTransformerQName(), valueQuery
                                                .getQueryString());
                                    }
                                } else {
                                    // Queries like: "$dataObject/dataElement/value..."

                                    List<DataValue> dataValues = elementInstance.getDataValues();

                                    if (dataValues != null && !dataValues.isEmpty()) {
                                        if (valueQuery.specifiesPropertySelection()) {
                                            // Queries like: "$dataObject/dataElement/value?property"
                                            if (dataElement.isCollectionElement()) {

                                                switch (valueQuery.getProperty()) {
                                                    case URL:
                                                        // Get all data values associated to the data element instance
                                                        io.swagger.trade.client.jersey.model.DataValueArrayWithLinks
                                                                dataValueArrayWithLinks = null;
                                                        try {
                                                            dataValueArrayWithLinks = this.dataValueApi.getDataValues
                                                                    (elementInstance.getIdentifier(), null);
                                                        } catch (io.swagger.trade.client.jersey.ApiException e) {
                                                            logger.error("Resolution of the query statement '" + valueQuery
                                                                    .getQueryString() + "' for transformation '" + this
                                                                    .transformation.getTransformerQName() +
                                                                    "' caused an exception. Therefore, the empty string is used as" +
                                                                    " query result value which might lead to undesired effects.", e);
                                                        }

                                                        if (dataValueArrayWithLinks != null && dataValueArrayWithLinks.getDataValues() != null) {
                                                            // Retrieve the URLs of the data values, concatenate them
                                                            // and use the resulting string as query result
                                                            StringBuilder builder = new StringBuilder();
                                                            for (DataValueWithLinks dataValue :
                                                                    dataValueArrayWithLinks.getDataValues()) {
                                                                builder.append(dataValue.getDataValue().getHref());
                                                                builder.append(",");
                                                            }
                                                            // Remove the unnecessary "," at the end of the string
                                                            builder.deleteCharAt(builder.lastIndexOf(","));
                                                            result = builder.toString();
                                                        }
                                                        break;
                                                    case SIZE:
                                                        // The size is the number of data values associated to
                                                        // the data element instance
                                                        result = String.valueOf(elementInstance.getNumberOfDataValues());
                                                        break;
                                                    default:
                                                        break;
                                                }
                                            } else {
                                                switch (valueQuery.getProperty()) {
                                                    case URL:
                                                        // Get the data value associated to the data element instance
                                                        io.swagger.trade.client.jersey.model.DataValueArrayWithLinks
                                                                dataValueArrayWithLinks = null;
                                                        try {
                                                            dataValueArrayWithLinks = this.dataValueApi.getDataValues
                                                                    (elementInstance.getIdentifier(), null);
                                                        } catch (io.swagger.trade.client.jersey.ApiException e) {
                                                            logger.error("Resolution of the query statement '" + valueQuery
                                                                    .getQueryString() + "' for transformation '" + this
                                                                    .transformation.getTransformerQName() +
                                                                    "' caused an exception. Therefore, the empty string is used as" +
                                                                    " query result value which might lead to undesired effects.", e);
                                                        }

                                                        if (dataValueArrayWithLinks != null && dataValueArrayWithLinks
                                                                .getDataValues() != null && !dataValueArrayWithLinks
                                                                .getDataValues().isEmpty()) {
                                                            // Retrieve the URL of the data value
                                                            // and use it as query result
                                                            result = dataValueArrayWithLinks.getDataValues().get(0)
                                                                    .getDataValue().getHref();
                                                        }

                                                        break;
                                                    case SIZE:
                                                        // The size is by default 1
                                                        result = "1";
                                                        break;
                                                    default:
                                                        break;
                                                }
                                            }
                                        } else {
                                            // Queries like: "$dataObject/dataElement/value"
                                            if (dataElement.isCollectionElement()) {
                                                // Queries like: "$dataObject/dataElement/value" do not point to a usable value,
                                                // if the data element is a collection element
                                                logger.warn("The transformation with name '{}' " +
                                                        "specifies a query statement ({}) which does not point to a usable data value. Therefore," +
                                                        " the empty string is used as value which might lead to undesired " +
                                                        "effects. Please correct the respective query statement in the underlying data dependency graph " +
                                                        "model.", this.transformation.getTransformerQName(), valueQuery.getQueryString());
                                            } else {
                                                DataValue value = dataValues.get(0);

                                                try {
                                                    // Retrieve the data of the data value and use it as the query result
                                                    result = new String(value.getData());
                                                } catch (Exception e) {
                                                    logger.error("Resolution of the query statement '" + valueQuery
                                                            .getQueryString() + "' for transformation '" + this
                                                            .transformation.getTransformerQName() +
                                                            "' caused an exception. Therefore, the empty string is used as" +
                                                            " query result value which might lead to undesired effects.", e);
                                                }
                                            }
                                        }
                                    } else {
                                        logger.error("The transformation with name '{}' " +
                                                        "specifies a query statement ({}) that could not be resolved to " +
                                                        "an actual value for the given instance context with correlation " +
                                                        "properties '{}'. Therefore, the empty string is used as" +
                                                        " query result value which might lead to undesired effects.",
                                                this.transformation.getTransformerQName(), valueQuery
                                                        .getQueryString(), instanceContext.getCorrelationProperties());
                                    }
                                }

                            } else {
                                if (valueQuery.specifiesPropertySelection()) {
                                    // Queries like: "$dataObject/dataElement?property"

                                    switch (valueQuery.getProperty()) {
                                        case URL:
                                            io.swagger.trade.client.jersey.model.DataElementInstanceWithLinks instanceWithLinks =
                                                    null;
                                            try {
                                                instanceWithLinks = this.dataElementInstanceApi
                                                        .getDataElementInstance(elementInstance.getIdentifier());
                                            } catch (io.swagger.trade.client.jersey.ApiException e) {
                                                logger.error("Resolution of the query statement '" + valueQuery
                                                        .getQueryString() + "' for transformation '" + this
                                                        .transformation.getTransformerQName() +
                                                        "' caused an exception. Therefore, the empty string is used as" +
                                                        " query result value which might lead to undesired effects.", e);
                                            }

                                            if (instanceWithLinks != null && instanceWithLinks.getInstance() != null) {
                                                // Retrieve the URL of the data element instance and use it as the query
                                                // result
                                                result = instanceWithLinks.getInstance().getHref();
                                            }
                                            break;
                                        case SIZE:
                                            // The size is by default 1
                                            result = "1";
                                            break;
                                        default:
                                            break;
                                    }
                                } else {
                                    // Queries like: "$dataObject/dataElement" do not point to a usable value
                                    logger.warn("The transformation with name '{}' " +
                                            "specifies a query statement ({}) which does not point to a usable data value. Therefore," +
                                            " the empty string is used as value which might lead to undesired " +
                                            "effects. Please correct the respective query statement in the underlying data dependency graph " +
                                            "model.", this.transformation.getTransformerQName(), valueQuery.getQueryString());
                                }
                            }
                        } else {
                            logger.error("The transformation with name '{}' " +
                                            "specifies a data element '{}' in a query ({}) for which no instance " +
                                            "could be resolved. Therefore, the empty string is used as value which " +
                                            "might lead to undesired effects.",
                                    this.transformation.getTransformerQName(), valueQuery.getDataObjectName(), valueQuery.getQueryString());
                        }
                    } else {
                        if (valueQuery.specifiesPropertySelection()) {
                            // Queries like: "$dataObject?property"

                            switch (valueQuery.getProperty()) {
                                case URL:
                                    DataObjectInstanceWithLinks instanceWithLinks = null;
                                    try {
                                        DataObjectInstance objInstance = dataObject.getDataObjectInstanceByCorrelationProps
                                                (instanceContext.getCorrelationProperties());

                                        if (objInstance != null) {
                                            instanceWithLinks = this.dataObjectInstanceApi.getDataObjectInstance(objInstance
                                                    .getIdentifier());
                                        }
                                    } catch (io.swagger.trade.client.jersey.ApiException e) {
                                        logger.error("Resolution of the query statement '" + valueQuery
                                                .getQueryString() + "' for transformation '" + this
                                                .transformation.getTransformerQName() +
                                                "' caused an exception. Therefore, the empty string is used as" +
                                                " query result value which might lead to undesired effects.", e);
                                    }

                                    if (instanceWithLinks != null && instanceWithLinks.getInstance() != null) {
                                        // Retrieve the URL of the data object instance and use it as the query
                                        // result
                                        result = instanceWithLinks.getInstance().getHref();
                                    }

                                    break;
                                case SIZE:
                                    // The size is by default "1"
                                    result = "1";
                                    break;
                                default:
                                    break;
                            }
                        } else {
                            // Queries like: "$dataObject" do not point to a usable value
                            logger.warn("The transformation with name '{}' " +
                                    "specifies a query statement ({}) which does not point to a usable data value. Therefore," +
                                    " the empty string is used as value which might lead to undesired " +
                                    "effects. Please correct the respective query statement in the underlying data dependency graph " +
                                    "model.", this.transformation.getTransformerQName(), valueQuery.getQueryString());
                        }
                    }
                } else {
                    logger.error("The transformation with name '{}' " +
                                    "specifies a data object '{}' in a query ({}) which could not be resolved. Please" +
                                    " check if the underlying data model specifies the referenced data object of the query.",
                            this.transformation.getTransformerQName(), valueQuery.getDataObjectName(), valueQuery.getQueryString());
                }
            }
        } else {
            logger.error("The transformation with name '{}' " +
                    "specifies an invalid query statement ({}) for resolving a input parameter value of the " +
                    "transformation. Therefore, the empty string is used as value which might lead to undesired " +
                    "effects. Please correct the respective query statement in the underlying data dependency graph " +
                    "model.", this.transformation.getTransformerQName(), valueQuery.getQueryString());
        }

        return result;
    }

    private List<RequestInputFile> resolveInputFiles(Transformation transformation, DataElementInstance
            instanceContext) {
        List<RequestInputFile> result = new ArrayList<>();

        if (transformation.getInputFiles() != null && !transformation.getInputFiles().isEmpty()) {
            // TODO: Remove single-source restriction in future
            if (transformation.getInputFiles().size() == 1) {
                // Map the source data elements to the input files required by the transformation
                for (InputFile file : transformation.getInputFiles()) {

                    io.swagger.trade.client.jersey.model.DataValue value = resolveSimpleDataValue(this.transformation
                            .getSource(), instanceContext);

                    if (value != null) {
                        // Translate the data element to an input file
                        RequestInputFile input = new RequestInputFile();

                        input.setFormat(file.getFormat());
                        // Use the HREF of the resolved data value and append the "/data" suffix to get the actual
                        // data from it
                        input.setLink(value.getHref() + DATA_URL_SUFFIX);

                        result.add(input);
                    } else {
                        logger.error("Unable to resolve the value of the source data element '{}' which is required " +
                                        "as input for transformation '{}'", this.transformation.getSource().getIdentifier(),
                                this.transformation.getTransformerQName());
                    }
                }
            } else {
                logger.warn("The transformation with name '" + this.transformation.getTransformerQName() + "' " +
                        "requires more than one input data element. This is currently not supported by the TraDE " +
                        "Middleware.");
            }
        }

        return result;
    }

    private List<RequestInputFileSet> resolveInputFileSets(Transformation transformation, DataElementInstance
            instanceContext) {
        List<RequestInputFileSet> result = new ArrayList<>();

        if (transformation.getInputFileSets() != null && !transformation.getInputFileSets().isEmpty()) {
            // TODO: Remove single-source restriction in future
            if (transformation.getInputFileSets().size() == 1) {
                // Map the source data elements to the input file set required by the transformation
                for (InputFileSet fileSet : transformation.getInputFileSets()) {

                    io.swagger.trade.client.jersey.model.DataValueArray valueArray = resolveCollectionDataValue(this
                            .transformation
                            .getSource(), instanceContext);

                    if (valueArray != null && !valueArray.isEmpty()) {
                        // Translate the data value array to an input file set
                        RequestInputFileSet input = new RequestInputFileSet();

                        // Set the format common to all input data values, i.e., use the format of the first one
                        input.setFormat(fileSet.getFormat());

                        // Set the count of input data values based on the array size
                        input.setCount(valueArray.size());

                        for (io.swagger.trade.client.jersey.model.DataValueWithLinks value : valueArray) {
                            // Use the HREF of the resolved data value and append the "/data" suffix to get the actual
                            // data from it
                            input.addLinksToFilesItem(value.getDataValue().getHref() + DATA_URL_SUFFIX);
                        }

                        result.add(input);
                    } else {
                        logger.error("Unable to resolve the value of the source data element '{}' which is required " +
                                        "as input for transformation '{}'", this.transformation.getSource().getIdentifier(),
                                this.transformation.getTransformerQName());
                    }
                }
            } else {
                logger.warn("The transformation with name '" + this.transformation.getTransformerQName() + "' " +
                        "requires more than one input data element. This is currently not supported by the TraDE " +
                        "Middleware.");
            }
        }

        return result;
    }

    private io.swagger.trade.client.jersey.model.DataValue resolveSimpleDataValue(ABaseResource resource,
                                                                                  DataElementInstance instanceContext) {
        io.swagger.trade.client.jersey.model.DataValue result = null;

        if (resource instanceof DataElement) {
            // Get the correlation properties
            DataElementInstance elmInstance = ((DataElement) resource).getDataElementInstanceByCorrelation
                    (instanceContext.getCorrelationProperties());

            if (elmInstance != null) {
                try {
                    io.swagger.trade.client.jersey.model.DataValueArrayWithLinks dataValue = this.dataValueApi.getDataValues(elmInstance.getIdentifier(),
                            1);

                    if (dataValue.getDataValues() != null && !dataValue.getDataValues().isEmpty()) {
                        result = dataValue.getDataValues().get(0).getDataValue();
                    } else {
                        logger.warn("The transformation with name '" + this.transformation.getTransformerQName() + "' " +
                                "could not be triggered because the resolution of a required data value failed.");
                    }
                } catch (io.swagger.trade.client.jersey.ApiException e) {
                    logger.error("The transformation with name '" + this.transformation.getTransformerQName() + "' " +
                            "could not be triggered because the resolution of the input data value caused an " +
                            "exception.", e);
                }
            } else {
                logger.warn("The transformation with name '" + this.transformation.getTransformerQName() + "' " +
                        "could not be triggered because the resolution of a required data value failed.");
            }
        } else {
            logger.error("The transformation with name '" + this.transformation.getTransformerQName() + "' " +
                    "has a source resource that is not a data element. The TraDE Middleware currently only supports " +
                    "transformations between data elements.");
        }

        return result;
    }

    private io.swagger.trade.client.jersey.model.DataValueArray resolveCollectionDataValue(ABaseResource resource,
                                                                                           DataElementInstance instanceContext) {
        io.swagger.trade.client.jersey.model.DataValueArray result = null;

        if (resource instanceof DataElement) {
            if (((DataElement) resource).isCollectionElement()) {
                // Get the correlation properties
                DataElementInstance elmInstance = ((DataElement) resource).getDataElementInstanceByCorrelation
                        (instanceContext.getCorrelationProperties());

                if (elmInstance != null) {
                    try {
                        io.swagger.trade.client.jersey.model.DataValueArrayWithLinks dataValues = this.dataValueApi.getDataValues(elmInstance.getIdentifier(),
                                null);

                        result = dataValues.getDataValues();
                    } catch (io.swagger.trade.client.jersey.ApiException e) {
                        logger.error("The transformation with name '" + this.transformation.getTransformerQName() + "' " +
                                "could not be triggered because the resolution of the input data value caused an " +
                                "exception.", e);
                    }
                } else {
                    logger.error("The transformation with name '" + this.transformation.getTransformerQName() + "' " +
                            "could not be triggered because the resolution of a required data value failed.");
                }
            } else {
                logger.error("The transformation with name '" + this.transformation.getTransformerQName() + "' " +
                        "could not be triggered because it depends on a input file set but the specified data element" +
                        " does not provide the required collection of data values for the transformation.");
            }
        } else {
            logger.error("The transformation with name '" + this.transformation.getTransformerQName() + "' " +
                    "has a source resource that is not a data element. The TraDE Middleware currently only supports " +
                    "transformations between data elements.");
        }

        return result;
    }

    private String resolveAndPrepareResultEndpoint(Transformation transformation, DataElementInstance
            instanceContext) {
        String result = null;

        // TODO: Remove single-target restriction in future (use information from Transformation spec)

        // Try to resolve the endpoint of an existing data value that is associated to the underlying data
        // element instance of the target data element of the data transformation. If there is no such data value we
        // have to create a new one. It is also possible that the target data element is not yet instantiated, then
        // we also have to do this. Use the TraDE client and invoke all required operations through the web UI to
        // keep the transformation independent from the internal data management module.
        io.swagger.trade.client.jersey.model.DataValue value = null;
        ABaseResource target = this.transformation.getTarget();

        if (target instanceof DataElement) {
            DataElement dataElement = ((DataElement) target);
            // Resolve a matching data element instance based on the correlation properties
            DataElementInstance elmInstance = dataElement.getDataElementInstanceByCorrelation
                    (instanceContext.getCorrelationProperties());

            // Check if a data element instance exists, if not create one
            if (elmInstance == null) {
                // Create a new data element instance by instantiating the parent data object while reusing the
                // correlation properties of the instance context
                DataObjectInstanceData request = new DataObjectInstanceData();
                request.setCorrelationProperties(translateCorrelationProps(instanceContext.getCorrelationProperties()));
                request.setCreatedBy(INTERNAL);

                try {
                    this.dataObjectInstanceApi.addDataObjectInstance(((DataElement) target).getParent().getIdentifier(), request);
                } catch (io.swagger.trade.client.jersey.ApiException e) {
                    logger.error("The transformation with name '" + this.transformation.getTransformerQName() + "' " +
                            "could not be triggered because the instantiation of the target data element caused an " +
                            "exception.", e);
                }

                // Now we should be able to resolve a corresponding data element instance for the given correlation
                // properties
                elmInstance = ((DataElement) target).getDataElementInstanceByCorrelation
                        (instanceContext.getCorrelationProperties());
            }

            if (elmInstance != null) {
                // Check if the data element instance has already a data value associated, if not create and
                // associate a new one

                io.swagger.trade.client.jersey.model.DataValueArrayWithLinks dataValueArray = null;
                boolean notFound = false;
                try {
                    dataValueArray = this.dataValueApi.getDataValues(elmInstance
                                    .getIdentifier(),
                            null);
                } catch (io.swagger.trade.client.jersey.ApiException e) {
                    if (e.getCode() == 404) {
                        notFound = true;
                    } else {
                        logger.error("The transformation with name '" + this.transformation.getTransformerQName() + "' " +
                                "could not be triggered because the resolution of a required target data value caused" +
                                " an exception.", e);
                    }
                }

                if (notFound) {
                    // Create and associate a new data value
                    io.swagger.trade.client.jersey.model.DataValue dataValueData = new io.swagger.trade.client.jersey
                            .model.DataValue();
                    dataValueData.setType(dataElement.getType());
                    dataValueData.setContentType(dataElement.getContentType());
                    dataValueData.setName(dataElement.getName());
                    dataValueData.setCreatedBy(INTERNAL);

                    try {
                        io.swagger.trade.client.jersey.model.DataValueWithLinks newDataValue = this.dataValueApi
                                .associateDataValueToDataElementInstance(elmInstance.getIdentifier(), dataValueData);

                        value = newDataValue.getDataValue();
                    } catch (io.swagger.trade.client.jersey.ApiException e) {
                        logger.error("The transformation with name '" + this.transformation.getTransformerQName() + "' " +
                                "could not be triggered because the resolution of a required target data value caused" +
                                " an exception.", e);
                    }
                } else {
                    if (dataValueArray != null) {
                        // TODO: Support multiple result data values for transformations in future
                        // Use the first data value of the array by default
                        value = dataValueArray.getDataValues().get(0).getDataValue();
                    }
                }

                if (value != null) {
                    // Use the HREF of the resolved data value and append the "/data" suffix to enable the transformation
                    // framework to POST the transformation results to it
                    result = value.getHref() + DATA_URL_SUFFIX;
                } else {
                    logger.error("The transformation with name '" + this.transformation.getTransformerQName() + "' " +
                            "could not be triggered because the resolution of a required target data value failed.");
                }
            } else {
                logger.error("The transformation with name '" + this.transformation.getTransformerQName() + "' " +
                        "could not be triggered because the resolution of a required target data value failed.");
            }
        } else {
            logger.error("The transformation with name '" + this.transformation.getTransformerQName() + "' " +
                    "has a source resource that is not a data element. The TraDE Middleware currently only supports " +
                    "transformations between data elements.");
        }

        return result;
    }

    private static CorrelationPropertyArray translateCorrelationProps(HashMap<String, String> correlationProperties) {
        CorrelationPropertyArray result = new CorrelationPropertyArray();

        for (String key : correlationProperties.keySet()) {
            CorrelationProperty prop = new CorrelationProperty();

            prop.setKey(key);
            prop.setValue(correlationProperties.get(key));

            result.add(prop);
        }

        return result;
    }

    @Override
    protected void doStart() throws Exception {
        this.appApi = new ApplicationsApi(this.transformationClient);
        this.transformationTaskApi = new TasksApi(this.transformationClient);
        this.transformationApi = new TransformationsApi(this.transformationClient);

        // Check if we have to resolve an application that provides the required transformation or if this
        // information is already present
        if (this.transformation.getHdtTransformer() == null) {
            // Search for a transformation
            List<Transformation> transformations = this.transformationApi.hdtappsApiFindTransformations(this.transformation.getTransformerQName(), null, null, null, null, null);

            if (transformations != null && !transformations.isEmpty()) {
                // We use by default the first element
                this.transformation.setHdtTransformer(transformations.get(0));
            }
        }

        // Create new clients to connect to the TraDE Middleware API in order to resolve external URLs, create
        // required data values, instantiate data objects, etc.
        dataValueApi = new DataValueApi(tradeClient);
        dataObjectInstanceApi = new DataObjectInstanceApi(tradeClient);
        dataElementInstanceApi = new DataElementInstanceApi(tradeClient);
    }

    @Override
    protected void doStop() throws Exception {
        this.transformationTaskApi = null;
    }
}
