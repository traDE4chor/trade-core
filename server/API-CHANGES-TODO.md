# Things to clarify
  * Should we introduce an ENUM with the currently supported data types for Data Values?
  * Should we handle data exchange with streams instead of ``byte[]`` (``DataValuesApiServiceImpl.pullDataValue()``)?
    * http://stackoverflow.com/questions/10326460/how-to-avoid-outofmemoryerror-when-uploading-a-large-file-using-jersey-client/31140433#31140433
    * http://stackoverflow.com/questions/23701106/how-jersey-2-client-can-send-input-output-binary-stream-to-server-and-vise-versa/23701359#23701359
    * http://stackoverflow.com/questions/10587561/password-protected-zip-file-in-java/32253028#32253028
    * http://stackoverflow.com/questions/3496209/input-and-output-binary-streams-using-jersey/28479669#28479669
    * If we do not switch to streams the ``Content-Length`` header for PULL requests could be removed or at least 
    marked as not being *required*? 

# General API ToDos
  * [ ] Improve all operation definitions so that each of them has a short **summary and a description**
  * [ ] Synchronize and align all descriptions
  * [ ] Improve all methods/definitions regarding the creation and handling of DataObject and DataElement instances!
  * [ ] Introduce methods for the management of 'links' between Data Element Instances and Data Values
  * [ ] Improve Error handling, e.g., introduce error codes, fill Error responses properly with human readable 
  information

# DataValues
  * [ ] Extend the DataValue resource (and related API classes) with corresponding data to enable the handling of 
  simple/atomic data wrapped as byte[]
  * [ ] Extend data value test cases (``DataValueTestHelper.java``), so that not only 'happy paths' are tested!