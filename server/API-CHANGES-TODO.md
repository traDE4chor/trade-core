# Things to clarify
  * Should we introduce UUID-based IDs for all model classes instead of using URNs as identifiers?
  * Should we introduce an ENUM with the currently supported data types for Data Values?
  * Should we handle data exchange with streams instead of ``byte[]`` (``DataValuesApiServiceImpl.pullDataValue()``)?
    * http://stackoverflow.com/questions/10326460/how-to-avoid-outofmemoryerror-when-uploading-a-large-file-using-jersey-client/31140433#31140433
    * http://stackoverflow.com/questions/23701106/how-jersey-2-client-can-send-input-output-binary-stream-to-server-and-vise-versa/23701359#23701359
    * http://stackoverflow.com/questions/10587561/password-protected-zip-file-in-java/32253028#32253028
    * http://stackoverflow.com/questions/3496209/input-and-output-binary-streams-using-jersey/28479669
    #28479669

# General API ToDos
  * [ ] Add '500 Server Error' where applicable and useful
  * [ ] Synchronize and align all descriptions
  * [ ] Improve all methods/definitions regarding the creation and handling of DataObject and DataElement instances!
  * [ ] Introduce methods for the management of 'links' between Data Element Instances and Data Values
  * [ ] Improve Error handling, e.g., introduce error codes, fill Error responses properly with human readable 
  information

# DataValues
  * [x] Remove 404 response on GET /dataValues
  * [x] Add a DELETE method to delete data values (and their associated data).
  * [x] Add 500 response to GET /dataValues
  * [x] Add 500 response to GET /dataValues/{dataValueId}
  * [x] Remove Content-Disposition headers: '/dataValues/{dataValueId}/pull' 
  * [ ] Extend the DataValue resource (and related API classes) with corresponding data to enable the handling of 
  simple/atomic data wrapped as byte[]
    * For example, a client should be able to parse the byte[] returned by the API with corresponding logic into an 
    Object of the used programming language, e.g., a String or a List of Integers in Java...
    * At the end of the day, the byte[] is always transporting a set of characters which are wrapping/representing a 
    single value or a collection of values. Therefore, we need some serialization format (e.g. using patterns) which 
    can be attached to the DataValue resource.  
  * [ ] Extend data value test cases (``DataValueTestHelper.java``), so that not only 'happy paths' are tested!
  

## Enable the specification of names for DataValues
  * [x] Extend API: DataValueRequest with "name" property to allow users the specification of names
  * [x] Introduce new DataValueUpdateRequest for updating data values through PUT
  * [x] Change internal DataValue model (in ``models`` module)
    * Introduced ``humanReadableName`` property, maybe this requires further improvement?
  * [x] Change all related method signatures and implementations
  * [x] Update the ResourceTransformationUtils
  
