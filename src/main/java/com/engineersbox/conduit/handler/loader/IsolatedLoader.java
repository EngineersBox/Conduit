package com.engineersbox.conduit.handler.loader;

public class IsolatedLoader {

    // TODO: Create ClassLoader for .class files of precompiled lua java bytecode
    //       https://stackoverflow.com/questions/6219829/method-to-dynamically-load-java-class-files
    //       Instantiated class should be an instance of VarArgFunction with onInvoke(Varargs)
    //       as a member method. This should return a LuaTable as the first value, on which
    //       the LuaTable#invokeMethod(name, args) method can be called to invoke a handler
    //       by name with args
}
