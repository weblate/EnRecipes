import { NativeScriptConfig } from "@nativescript/core"

export default {
  id: "com.vishnuraghav.enrecipes",
  appResourcesPath: "app/resources",
  android: {
    v8Flags: "--expose_gc",
    markingMode: "none",
    codeCache: true,
  }
  // appPath: "app",
} as NativeScriptConfig
