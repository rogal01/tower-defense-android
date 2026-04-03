# Add project specific ProGuard rules here.

# ── Android framework ─────────────────────────────────────────────────────────
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable

# Keep all Activity subclasses (referenced by name in AndroidManifest.xml)
-keep public class * extends android.app.Activity
-keep public class * extends androidx.appcompat.app.AppCompatActivity

# Keep Views with custom constructors (used in XML layouts / GameView)
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# ── Kotlin ────────────────────────────────────────────────────────────────────
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings { <fields>; }
-keepclassmembers class kotlin.Lazy { *; }

# Keep Kotlin companion objects
-keepclassmembers class * {
    public static ** Companion;
}

# ── Enums ─────────────────────────────────────────────────────────────────────
# Enum names are saved to SharedPreferences (e.g. TowerType.ARROW.name, GameMode.name)
# so they must NOT be obfuscated.
-keepclassmembers enum com.example.myapp.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    public final java.lang.String name();
    public final int ordinal();
}
-keepnames enum com.example.myapp.**

# ── SharedPreferences keys ─────────────────────────────────────────────────────
# String constants used as prefs keys are inlined by R8 — no extra rule needed.

# ── Game data classes ─────────────────────────────────────────────────────────
# Keep field names on data classes that may be accessed reflectively or serialized
-keepclassmembers class com.example.myapp.** {
    public <fields>;
}

# ── Suppress warnings for libraries we don't use ─────────────────────────────
-dontwarn javax.annotation.**
-dontwarn org.codehaus.mojo.**

# ── Keep R class ──────────────────────────────────────────────────────────────
-keepclassmembers class **.R$* {
    public static <fields>;
}
