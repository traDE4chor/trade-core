<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<trade:notificationMessage xmlns:trade="http://org.trade.core/notifications">
    <trade:notification name="${notification.name}">
        <trade:description>
            The notification that is responsible for sending this notification message.
        </trade:description>
    </trade:notification>

    <trade:triggerEvent>
        <trade:description>
            The event that matches the specified resource event filter criteria of the above referenced notification.
        </trade:description>
        <trade:eventType>${notificationSource.getType()}</trade:eventType>
        <trade:timestamp>${notificationSource.timestamp?datetime}</trade:timestamp>
        <trade:resourceIdentifier>${notificationSource.identifier}</trade:resourceIdentifier>
        <trade:resourceModelClass>${notificationSource.modelClass}</trade:resourceModelClass>
        <trade:eventAsString>
            ${notificationSource.toString()}
        </trade:eventAsString>
    </trade:triggerEvent>

    <trade:resource id="${notificationSource.identifier}" type="${notificationSource.modelClass}">
        <trade:description>
            The resource that triggers the notification through a corresponding event, e.g., if the state of the
            resource has changed.
        </trade:description>
        <#if notification.resourceURL??>
        <trade:resourceURL>${notification.resourceURL}</trade:resourceURL>
        </#if>
        <#-- Check if the notification is registered for a specific resource -->
        <#if notification.resource??>
        <trade:resourceAsString>
            ${notification.resource.toString()}
        </trade:resourceAsString>
        </#if>
    </trade:resource>
</trade:notificationMessage>