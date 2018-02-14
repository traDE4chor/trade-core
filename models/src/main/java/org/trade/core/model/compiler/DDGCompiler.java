/*
 * Copyright 2017 Michael Hahn
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

package org.trade.core.model.compiler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.model.ABaseResource;
import org.trade.core.model.ModelConstants;
import org.trade.core.model.data.DataElement;
import org.trade.core.model.data.DataModel;
import org.trade.core.model.data.DataObject;
import org.trade.core.model.dataTransformation.DataTransformation;
import org.trade.core.model.ddg.*;
import org.trade.core.model.lifecycle.LifeCycleException;
import org.trade.core.model.utils.DDGUtils;
import org.trade.core.query.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides functionality to compile data dependency graphs. This means a serialized, XML-based
 * representation is used as input which will be resolved into a collection of hierarchically related model objects.
 * <p>
 * Created by hahnml on 02.11.2016.
 */
public class DDGCompiler extends ACompiler {

    private Logger logger = LoggerFactory.getLogger("org.trade.core.model.compiler.DDGCompiler");

    private String ddgId = "";

    private String targetNamespace = "";

    private DataModel dataModel = null;

    private Map<String, ABaseResource> compiledResourcesCache = new HashMap<>();

    private List<DataTransformation> dataTransformations;

    // TODO: 10.04.2017 Requires rework if we restructure/refactor data dependence graph and data model schemas (XSD files)...

    public void compile(String ddgId, String entity, byte[] serializedDDG) throws CompilationException {
        DataDependenceGraph ddgDef = null;
        try {
            ddgDef = DDGUtils.unmarshalGraph(serializedDDG);

            compile(ddgId, entity, ddgDef);

            // Write all identified issues to the log
            writeCompilationIssuesToLog(logger);
        } catch (CompilationException e) {
            logger.warn("The compilation of data dependency graph '" + this.ddgId + "' ended with errors.", e);

            // Output the compilation issues also to the log as warnings
            writeCompilationIssuesToLog(logger);

            // Simply forward the exception
            throw e;
        } catch (Exception e) {
            logger.error("The compilation of data dependency graph '" + this.ddgId + "' caused an " +
                    "exception.", e);

            // Wrap any other kind of exception into a CompilationException
            throw new CompilationException("The compilation of data dependency graph '" + this.ddgId + "' caused an " +
                    "exception.", e,
                    this.compilationIssues);

        }
    }

    public void compile(String ddgId, String entity, DataDependenceGraph ddgDefinition) throws CompilationException {
        this.ddgId = ddgId;
        String targetNamespace = "";
        String name = "";

        if (ddgDefinition == null) {
            logger.error("The data dependency graph '{}' could not be deserialized successfully. Please check if the " +
                    "provided XML file is compatible with the underlying XSD schema for data dependency graphs.", this
                    .ddgId);

            throw new CompilationException("The data dependency graph '" + this.ddgId + "' could not be deserialized " +
                    "successfully. Please check if the provided XML file is compatible with the underlying XSD schema" +
                    " for data dependency graphs.", this.compilationIssues);
        }

        if (ddgDefinition.getDataObjects() == null || ddgDefinition.getDataObjects().getDataObject() == null
                || ddgDefinition.getDataObjects().getDataObject().isEmpty()) {
            logger.error("The data dependency graph '{}' does not specify any data objects. Please improve your" +
                    "definition so that it contains one or more data objects that should be managed by the TraDE " +
                    "middleware", this.ddgId);

            throw new CompilationException("The data dependency graph '" + this.ddgId + "' does not specify any data " +
                    "objects. Please improve your definition so that it contains one or more data objects that should" +
                    " be managed by the TraDE middleware", this.compilationIssues);
        }

        if (ddgDefinition.getTargetNamespace() == null || ddgDefinition.getTargetNamespace().isEmpty()) {
            // Reject the model if no namespace is specified
            logger.error("The given data dependency graph does not specify a target namespace which is required " +
                    "for the identification of the produced data model. Please update the data dependency graph so " +
                    "that it specifies a target namespace.");

            throw new CompilationException("The given data dependency graph does not specify a target namespace which" +
                    " is required for the identification of the produced data model. Please update the data " +
                    "dependency graph so that it specifies a target namespace.", this.compilationIssues);
        } else {
            if (ddgDefinition.getName() == null || ddgDefinition.getName().isEmpty()) {
                String msg = "The data dependency graph with target namespace '" + ddgDefinition.getTargetNamespace() +
                        "' does not specify a name. We therefore use the specified entity '" + entity + "' as name. " +
                        "Please specify a name in the future.";
                CompilationIssue issue = new CompilationIssue(CompilationIssueType.MissingName, msg);
                this.compilationIssues.add(issue);

                name = entity;
            } else {
                name = ddgDefinition.getName();
            }

            targetNamespace = ddgDefinition.getTargetNamespace();
        }

        this.targetNamespace = targetNamespace;

        // Create a new DataModel to which we can add the elements translated in the following
        this.dataModel = new DataModel(entity, name, targetNamespace);

        List<DataObject> dataObjects = new ArrayList<>();
        for (DataObjectType dataObject : ddgDefinition.getDataObjects().getDataObject()) {
            dataObjects.add(compile(dataObject, entity));
        }

        // Try to initialize the data model after everything is compiled successfully
        try {
            this.dataModel.initialize(dataObjects);
        } catch (LifeCycleException e) {
            logger.error("The generated data model '{}' could not be initialized successfully. Please check if " +
                            "all data objects and data elements are generated and initialized correctly.",
                    this.dataModel.getIdentifier());

            throw new CompilationException("The generated data model '" + this.dataModel.getIdentifier() + "' could " +
                    "not be initialized successfully. Please check if all data objects and data elements are " +
                    "generated and initialized correctly.", this.compilationIssues);
        }

        // Compile specified data transformations
        this.dataTransformations = new ArrayList<>();
        for (DataDependenceEdgeType edge : ddgDefinition.getDependenceEdges().getDependenceEdge()) {
            DataTransformation transf = compile(edge);
            if (transf != null) {
                dataTransformations.add(transf);
            }
        }

        // TODO: 10.04.2017 Add compilation support for PROCESSORS and DEPENDENCE EDGES in general
    }

    private DataObject compile(DataObjectType dataObject, String entity) throws CompilationException {
        DataObject compiledDataObject = null;

        String name = null;
        String identifier = null;

        if (dataObject.getName() == null || dataObject.getName().isEmpty()) {
            // Reject the model if no name is specified
            logger.error("A data object does not specify a name. Please update the data dependency graph so that for " +
                    "each data object at least a name is specified.");

            throw new CompilationException("A data object does not specify a name. Please update the data dependency " +
                    "graph so that for each data object at least a name is specified.", this.compilationIssues);
        } else {
            if (dataObject.getIdentifier() == null || dataObject.getIdentifier().isEmpty()) {
                String msg = "Data object '" + dataObject.getName() + "' does not specify an identifier. We " +
                        "therefore generate a random identifier. Please specify an identifier value if you want to" +
                        "predefine the identifier of a data object in the future.";
                CompilationIssue issue = new CompilationIssue(CompilationIssueType.MissingName, msg);
                this.compilationIssues.add(issue);
            } else {
                identifier = dataObject.getIdentifier();
            }

            name = dataObject.getName();
        }

        if (dataObject.getDataElements() == null || dataObject.getDataElements().getDataElement() == null
                || dataObject.getDataElements().getDataElement().isEmpty()) {
            String msg = "Data object '" + name + "' does not specify any data elements. Please add " +
                    "one or more data elements to this data object to make it useable.";
            CompilationIssue issue = new CompilationIssue(CompilationIssueType.MissingElements, msg);
            this.compilationIssues.add(issue);
        }

        // Create a new DataObject to which we can add data elements translated in the following
        if (identifier != null) {
            // Use the provided identifier
            compiledDataObject = new DataObject(this.dataModel, identifier, entity, name);
        } else {
            // Use the generated identifier from BaseResource
            compiledDataObject = new DataObject(this.dataModel, entity, name);
        }

        // Add the new data object to the cache
        compiledResourcesCache.put(compiledDataObject.getIdentifier(), compiledDataObject);

        for (DataElementType dataElement : dataObject.getDataElements().getDataElement()) {
            // Try to add the compiled data element to its data object
            DataElement compiledDataElement = compile(compiledDataObject, dataElement, entity);
            try {
                compiledDataElement.initialize();
            } catch (LifeCycleException e) {
                logger.error("The generated data element '{}' could not be added to data object '{}'. " +
                        "Please check if the corresponding data element is generated and initialized " +
                        "correctly.", compiledDataElement.getName(), compiledDataObject.getName());

                throw new CompilationException("The generated data element '" + compiledDataElement.getName() + "' " +
                        "could not be added to data object '" + compiledDataObject.getName() + "'. " +
                        "Please check if the corresponding data element is generated and initialized correctly.",
                        this.compilationIssues);
            } catch (Exception e) {
                logger.error("The generated data element '{}' could not be initialized. " +
                                "Please check if the corresponding data element is generated correctly.",
                        compiledDataElement.getName());

                throw new CompilationException("The generated data element '" + compiledDataElement.getName() + "' " +
                        "could not be initialized. Please check if the corresponding data element is generated " +
                        "correctly.",
                        this.compilationIssues);
            }
        }

        return compiledDataObject;
    }

    private DataElement compile(DataObject compiledDataObject, DataElementType dataElement, String entity) throws
            CompilationException {
        DataElement compiledElement = null;

        String name = null;
        String identifier = null;

        if (dataElement.getName() == null || dataElement.getName().isEmpty()) {
            // Reject the model if no name is specified
            logger.error("A data element does not specify a name. Please update the data dependency graph so that for" +
                    " each data element at least a name is specified.");

            throw new CompilationException("A data element does not specify a name. Please update the data dependency" +
                    " graph so that for each data element at least a name is specified.", this.compilationIssues);
        } else {
            if (dataElement.getIdentifier() == null || dataElement.getIdentifier().isEmpty()) {
                String msg = "Data element '" + dataElement.getName() + "' does not specify an identifier. We " +
                        "therefore generate a random identifier. Please specify an identifier value if you want to" +
                        "predefine the identifier of a data element in the future.";
                CompilationIssue issue = new CompilationIssue(CompilationIssueType.MissingName, msg);
                this.compilationIssues.add(issue);
            } else {
                identifier = dataElement.getIdentifier();
            }

            name = dataElement.getName();
        }

        boolean isCollection = dataElement.isIsCollection() == null
                ? false : dataElement.isIsCollection();

        // Create a new DataElement
        if (identifier != null) {
            // Use the provided identifier
            compiledElement = new DataElement(compiledDataObject, identifier, entity, name, isCollection);
        } else {
            // Use the generated identifier from BaseResource
            compiledElement = new DataElement(compiledDataObject, entity, name, isCollection);
        }

        // Add the new data element to the cache
        compiledResourcesCache.put(compiledElement.getIdentifier(), compiledElement);

        boolean isBinaryType = false;
        if (dataElement.getType() == null || dataElement.getType().isEmpty()) {
            String msg = "Data element '" + dataElement.getName() + "' does not specify a type. This might have " +
                    "implications during its later use, e.g., when assigning a data value to an instance of this data" +
                    " element. Please specify a type value in future.";
            CompilationIssue issue = new CompilationIssue(CompilationIssueType.MissingName, msg);
            this.compilationIssues.add(issue);
        } else {
            // Check if the type is "binary"
            if (dataElement.getType().toUpperCase().equals(ModelConstants.BINARY_TYPE)) {
                isBinaryType = true;
            }
        }
        compiledElement.setType(dataElement.getType());

        if ((dataElement.getContentType() == null || dataElement.getContentType().isEmpty()) && isBinaryType) {
            String msg = "Data element '" + dataElement.getName() + "' does not specify a content type for the " +
                    "related binary data. This might have implications during its later use, e.g., when assigning a " +
                    "data value to an instance of this data element. Please specify a type value in future.";
            CompilationIssue issue = new CompilationIssue(CompilationIssueType.MissingName, msg);
            this.compilationIssues.add(issue);
        }
        compiledElement.setContentType(dataElement.getContentType());

        return compiledElement;
    }

    private DataTransformation compile(DataDependenceEdgeType edge) throws
            CompilationException {
        DataTransformation compiledTransformation = null;

        DataTransformationType dataTransformation = edge.getTransformation();
        if (dataTransformation != null) {
            String name;

            if (dataTransformation.getTransformerID() == null || dataTransformation.getTransformerID().isEmpty()) {
                // No transformer is specified, there we cannot compile the data transformation
                logger.error("A data transformation does not specify a transformerID. Please update the data " +
                        "dependency graph so that for each data transformation of a data dependency edge is at least " +
                        "the transformerID specified.");

                throw new CompilationException("A data transformation does not specify a transformerID. Please update the data " +
                        "dependency graph so that for each data transformation of a data dependency edge is at least " +
                        "the transformerID specified.", this.compilationIssues);
            } else {
                if (dataTransformation.getName() == null || dataTransformation.getName().isEmpty()) {
                    String msg = "Data transformation '" + dataTransformation.getTransformerID() + "' does not " +
                            "specify a name. We therefore use the name of the data dependency edge that contains the " +
                            "transformation. Please specify a name value if you want to predefine the name of a" +
                            " data transformation in the future.";
                    CompilationIssue issue = new CompilationIssue(CompilationIssueType.MissingName, msg);
                    this.compilationIssues.add(issue);

                    // Use the name of the data dependency edge
                    name = edge.getName();
                } else {
                    // Use the provided name
                    name = dataTransformation.getName();
                }

                // Resolve source and target objects of the data dependence edge
                ABaseResource source = null;
                ABaseResource target = null;
                if (edge.getSource() != null) {
                    Object edgeSource = edge.getSource();

                    // TODO: For now, we only allow transformations on data dependency edges that connect data
                    // elements. This should be enhanced in future...
                    if (edgeSource instanceof DataElementType) {
                        DataElementType elm = (DataElementType) edgeSource;

                        // Resolve the corresponding compiled object from the cache
                        source = compiledResourcesCache.get(elm.getIdentifier());
                    }
                }

                if (edge.getTarget() != null) {
                    Object edgeTarget = edge.getTarget();

                    // TODO: For now, we only allow transformations on data dependency edges that connect data
                    // elements. This should be enhanced in future...
                    if (edgeTarget instanceof DataElementType) {
                        DataElementType elm = (DataElementType) edgeTarget;

                        // Resolve the corresponding compiled object from the cache
                        target = compiledResourcesCache.get(elm.getIdentifier());
                    }
                }

                if (source != null && target != null) {
                    // Create a new DataTransformation to which we can add transformation parameters in the following
                    compiledTransformation = new DataTransformation(name, dataTransformation
                            .getTransformerID());

                    compiledTransformation.setDataModel(this.dataModel);

                    if (dataTransformation.getParameters() != null) {
                        Map<String, Object> parameters = new HashMap<>();

                        for (TransformationParameterType param : dataTransformation.getParameters().getParameter()) {
                            // Check if the parameter value is a query
                            if (param.getParameterValue().startsWith("$")) {
                                Query query = Query.parseQuery(param.getParameterValue());

                                // Check if the query is valid or not
                                if (query.isValid()) {
                                    parameters.put(param.getParameterName(), query);
                                } else {
                                    String msg = "The parameter '" + param.getParameterName() + "' of data transformation" +
                                            " '" + compiledTransformation.getName() + "' does not " +
                                            "specify a valid query as value. The parameter is therefore ignored. Please " +
                                            "specify a valid query string as value if you want to enable the correct " +
                                            "resolution of a corresponding data value during run time in the future.";
                                    CompilationIssue issue = new CompilationIssue(CompilationIssueType.InvalidQuery, msg);
                                    this.compilationIssues.add(issue);
                                }
                            } else {
                                parameters.put(param.getParameterName(), param.getParameterValue());
                            }
                        }

                        // Set the defined parameters to the data transformation
                        compiledTransformation.setTransformerParameters(parameters);
                    }
                } else {
                    // Add an issue
                    String msg = "The data dependency edge '" + edge.getIdentifier() + "' specifies a data " +
                            "transformation from/to a resource other than a data element. The middleware only " +
                            "supports data transformations between data elements (source and target) at the " +
                            "moment. The data transformation is therefore ignored. Please " +
                            "adapt the underlying model if you want to enable the correct " +
                            "compilation and resolution of data transformations in the future.";
                    CompilationIssue issue = new CompilationIssue(CompilationIssueType.InvalidSourceType, msg);
                    this.compilationIssues.add(issue);
                }
            }
        }

        return compiledTransformation;
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    public DataModel getCompiledDataModel() {
        return this.dataModel;
    }

    public List<DataTransformation> getCompiledDataTransformations() {
        return this.dataTransformations;
    }
}
