Built over paw-project.sourceforge.net, this sub-project is focusing on having a proxy that instruments JavaScript,
to see how real browsers and htmlUnit process various web applications.

Requirements:
    - Java 6
    - Apache ant
    - JUnit 4 in $ANT_HOME/lib 

To run the server, type:
ant run-server

Which will start a local proxy listening at port 8080 (check conf/server.xml)

To shutdown the server, click the icon in the system tray.

To run the admin GUI, type:
ant run-gui

Currently, you can configure JavaScriptBeautifierFilter in conf/filter.xml to beautify all JavaScript files returned 
from the server side.

Roadmap:
    - More instrumentation ways, e.g.:
        - Log the entry of functions (partially implemented now by JavaScriptFunctionLogger)
        - Log all function invocations with their parameter values e.g. [method(var1, var2)], and log all 
          assignments e.g. [var x = y + z / method2(a, b())]
        - Log all method invocations all assignments of a specific function
        - ...

    - Logging should happen by sending XmlHttpRequest to non-existing URL in the web site under testing.
      That request will be intercepted by the proxy, to save the logged messages. 
    - Under the same proxy port, we can have a web application (GWT?) by which the user controls the instrumentation,
      sees the logs, compares real browsers and HtmlUnit behavior. Static web application is currently
      implemented by sunlabs.brazil.server.FileHandler