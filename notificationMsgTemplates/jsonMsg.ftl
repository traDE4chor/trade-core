{
  "notification": {
    "name": "${notification.name}",
    "description": "The notification that is responsible for sending this notification message."
  },
  "triggerEvent": {
    "description": "The event that matches the specified resource event filter criteria of the above referenced notification.",
    "eventType": "${notificationSource.getType()}",
    "timestamp": "${notificationSource.timestamp?datetime}",
    "resourceIdentifier": "${notificationSource.identifier}",
    "resourceModelClass": "${notificationSource.modelClass}",
    "eventAsString": "${notificationSource.toString()?replace("\n\t", ", ")}"
  },
  "resource": {
    "description": "The resource that triggers the notification through a corresponding event, e.g., if the state of the resource has changed.",
    "identifier": "${notificationSource.identifier}",
    <#-- Check if the notification is registered for a specific resource -->
    <#if notification.resource??>
    "resourceAsString": "${notification.resource.toString()?replace("\n\t", ", ")}",
    </#if>
    <#if notification.resourceURL??>
    "resourceURL": "${notification.resourceURL}"
    </#if>
  }
}