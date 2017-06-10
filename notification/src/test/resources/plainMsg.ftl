A registered notification [${notification.name}] was triggered by the following event:

################################
${notificationSource.toString()}
################################

<#-- Check if the notification is registered for a specific resource -->
<#if notification.resource??>
The resource for which the notification was registered looks like the following:

###################################
${notification.resource.toString()}
###################################
</#if>

<#if notification.resource?? && notification.resourceURL??>
The resource (${notificationSource.identifier}) which triggers the notification can be accessed through the following
 URL: ${notification.resourceURL}
<#elseif notification.resourceURL??>
The collection containing the resource (${notificationSource.identifier}) which triggers the notification can be accessed
through the following URL: ${notification.resourceURL}
<#else>
A resource URL is not available. Please goto the middleware and search for the resource manually using its
identifier: ${notificationSource.identifier}
</#if>