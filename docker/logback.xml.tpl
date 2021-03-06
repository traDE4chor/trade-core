<configuration>

    <timestamp key="startTimestamp" datePattern="yyyyMMddHHmmssSSS"/>

    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!-- Only log messages with level INFO or above to the log file -->
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>INFO</level>
        </filter>
        <file>logs/trade.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <!-- daily rollover -->
            <fileNamePattern>trade.%d{yyyy-MM-dd}.log</fileNamePattern>

            <!-- keep 10 days' worth of history capped at 200MB total size -->
            <maxHistory>10</maxHistory>
            <totalSizeCap>200MB</totalSizeCap>
        </rollingPolicy>

        <encoder>
            <pattern>%date %level [%thread] %logger{10} [%file:%line] %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="ROUTE_ERROR_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/camel-deadLetterQueue.${startTimestamp}.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <!-- daily rollover -->
            <fileNamePattern>camel-error.%d{yyyy-MM-dd}.log</fileNamePattern>

            <!-- keep 10 days' worth of history capped at 200MB total size -->
            <maxHistory>10</maxHistory>
            <totalSizeCap>200MB</totalSizeCap>
        </rollingPolicy>

        <encoder>
            <pattern>%date %level [%thread] %logger{10} [%file:%line] %msg%n</pattern>
        </encoder>
    </appender>

    <!-- Configure the loggers for all errors in camel notification routes -->
    <logger name="org.trade.core.notification.management.camel.route" level="DEBUG">
        <appender-ref ref="ROUTE_ERROR_FILE" />
    </logger>

    <!-- Configure the loggers for all errors in camel data transformation routes -->
    <logger name="org.trade.core.data.transformation.management.camel.route" level="DEBUG">
        <appender-ref ref="ROUTE_ERROR_FILE" />
    </logger>

    <root level="{{ .Env.LOG_LEVEL }}">
        <appender-ref ref="FILE" />
        <appender-ref ref="STDOUT" />
    </root>

</configuration>
