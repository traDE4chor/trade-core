#
# Copyright 2016 Michael Hahn
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Configure the URL under which the API of the TraDE Middleware is exposed publicly, i.e., using the public IP of the
# underlying host so that the data transformation framework (HDTApps) can access data values to transform them and
# upload transformation results.
trade.url={{ .Env.TRADE_URL }}

# Configure the deployment mode. This means if the TraDE middleware is deployed and operated as a single node
# (mode=SINGLE_NODE) or as a connected network of multiple nodes using Hazelcast (mode=MULTI_NODE). In addition also
# a custom mode (mode=CUSTOM) can be specified. In this case users have to provide a list of corresponding references
# to classes which implement all required interfaces, e.g., IDataManager. We assume that the resulting implementation
# follows the singleton pattern using an enum with an 'INSTANCE' constant. The class SimpleDataManager.java can be
# used as a template for custom implementations.
deployment.mode=SINGLE_NODE
data.manager.class=org.trade.core.data.management.simple.SimpleDataManager

# Configure the embedded Jetty server
server.port.http=8081
server.port.https=8443
server.threads.min=50
server.threads.max=300
server.ssl.keystore.path=/ssl/keystore.jks
# You can specify the required passwords in plain text or use Jetty's password utility to generate obfuscated,
# checksummed or encrypted versions. See [https://wiki.eclipse.org/Jetty/Howto/Secure_Passwords] for more information.
server.ssl.keystore.password=someKeyStorePassword

# Configure MongoDB properties for the persistence of the cached data of Hazelcast
cache.db.url={{ .Env.MONGO_DB_URL }}
cache.db.name=tradeCacheDB

# Configure which mode ('FILE', 'DB' or 'CUSTOM') should be used to persist the data. In custom mode users have to
# provide a corresponding implementation of the IPersistenceProvider interface. We assume that the resulting
# implementation provides an no-argument constructor. The class FileSystemPersistence.java can be used as a template
# for custom implementations.
data.persistence.mode={{ .Env.PERSISTENCE_MODE }}
data.persistence.provider.class=org.trade.core.persistence.local.filesystem.FileSystemPersistence

# Database properties are only used/relevant in 'DB' mode
data.persistence.db.url={{ .Env.MONGO_DB_URL }}
data.persistence.db.name=tradeDataDB
# File properties are only used/relevant in 'FILE' mode.
# Configure the path in the filesystem where the data should be saved. Please escape all backslashes '\'
# in Windows file paths with a second '\', e.g., 'C:\\someDirectory\\trade'. Alternatively, you can also use
# single forward slashes instead.
data.persistence.file.directory={{ .Env.DATA_DIRECTORY }}

# Configure which notification mode ('CAMEL' or 'CUSTOM') should be used to realize the management and execution of
# data-related notifications, e.g., inform an external client if a data value is initialized and therefore available
# to read. In custom mode users have to provide a corresponding implementation of the INotificationManager interface.
# We assume that the resulting implementation follows the singleton pattern using an enum with an 'INSTANCE' constant.
# The class CamelNotificationManager.java can be used as a template for custom implementations.
notification.mode=CAMEL
notification.manager.class=org.trade.core.notification.management.camel.CamelNotificationManager
# INotifier implementations are configured and registered through the 'notifier.properties' file

# Configure the URL at which the API of the data transformation framework (HDTApps) can be accessed
hdtApps.framework.url={{ .Env.HDTApps_URL }}
