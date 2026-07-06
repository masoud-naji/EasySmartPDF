# Readable crash stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin metadata — required for reflection and coroutines dispatcher lookup
-keep class kotlin.Metadata { *; }

# Kotlin Coroutines — keep internal dispatcher factory names
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}

# Enum classes — values() and valueOf() are called via reflection by the runtime
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
