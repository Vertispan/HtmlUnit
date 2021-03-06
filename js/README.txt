This project is a Nashorn fork to suit HtmlUnit need.

It is currently based on Nashorn code from http://hg.openjdk.java.net/jdk8u/jdk8u-dev/nashorn/
as of 28 November 2016

jdk9 code depends on Java 9

- Global is the mandatory 'this' or 'top level' object in Nashorn, and it is with a one-to-one relation with Window.
HtmlUnit uses window, and Nashorn calls Global.
- ScriptConext is one per WebWindow.
- The entry point of dynamic linking is DynamicLinker (specially .relink()).


Main customizations:
- Function.arguments should always exist (can be detected by arguments.callee.arguments):
  fixed in Global.allocateArguments() and FunctionNode.needsArguments()
- Function.caller:
  fixed in CodeGenerator.scopeCall() to set CALLEE, and in ScriptFunction.findCallMethod()
- Handle return statements outside function:
  fixed in Parser.returnStatement()
- When setting property of another Global, don't use the current one
  fixed in SetMethodCreator.createGlobalPropertySetter
- ScriptObject.avoidObjectDetection() 