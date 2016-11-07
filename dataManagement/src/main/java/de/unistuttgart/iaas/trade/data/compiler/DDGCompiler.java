package de.unistuttgart.iaas.trade.data.compiler;

import de.unistuttgart.iaas.trade.model.data.DataElement;
import de.unistuttgart.iaas.trade.model.data.DataObject;
import de.unistuttgart.iaas.trade.model.ddg.DataDependenceGraphType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hahnml on 02.11.2016.
 */
public class DDGCompiler {

    Logger logger = LoggerFactory.getLogger("de.unistuttgart.iaas.trade.data.compiler.DDGCompiler");

    private DataDependenceGraphType ddgDef = null;

    private List<DataObject> dataObjects = new ArrayList<DataObject>();

    private HashMap<DataObject, List<DataElement>> dataElements = new HashMap<DataObject, List<DataElement>>();


}
