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
import org.trade.core.model.ModelConstants;
import org.trade.core.model.data.DataElement;
import org.trade.core.model.data.DataModel;
import org.trade.core.model.data.DataObject;
import org.trade.core.model.ddg.DataDependenceGraph;
import org.trade.core.model.ddg.DataElementType;
import org.trade.core.model.ddg.DataObjectType;
import org.trade.core.model.lifecycle.LifeCycleException;
import org.trade.core.model.utils.DDGUtils;

import java.util.ArrayList;
import java.util.List;

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

        // TODO: 10.04.2017 Add compilation support for PROCESSORS and DEPENDENCE EDGES
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

    public String getTargetNamespace() {
        return targetNamespace;
    }

    public DataModel getCompiledDataModel() {
        return this.dataModel;
    }
}
