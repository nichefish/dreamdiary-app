package dreamdiary.gradle

import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

/**
 * Spring 메시지 번들을 Vue에서 읽을 수 있는 JSON catalog로 변환합니다.
 */
abstract class GenerateI18nCatalogsTask extends DefaultTask {

    private static final Pattern MESSAGE_BUNDLE_PATTERN = Pattern.compile(/^messages(?:_([^.]+))?\.properties$/)

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract DirectoryProperty getMessagesDir()

    @OutputDirectory
    abstract DirectoryProperty getOutputDir()

    @TaskAction
    void generate() {
        File messagesDirectory = messagesDir.get().asFile
        File catalogDirectory = outputDir.get().asFile
        catalogDirectory.mkdirs()

        messagesDirectory
                .listFiles({ File file -> file.name ==~ /messages(?:_([^.]+))?\.properties/ } as FileFilter)
                ?.sort { File file -> file.name }
                ?.each { File source ->
                    def matcher = MESSAGE_BUNDLE_PATTERN.matcher(source.name)
                    if (!matcher.matches()) return

                    String locale = matcher.group(1) ?: 'default'
                    Properties props = new Properties()
                    source.withInputStream { stream ->
                        props.load(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    }

                    Map<String, String> catalog = new TreeMap<>()
                    props.stringPropertyNames().sort().each { String key ->
                        catalog[key] = props.getProperty(key)
                    }

                    String json = JsonOutput.prettyPrint(JsonOutput.toJson(catalog))
                    new File(catalogDirectory, "${locale}.json").setText(json + System.lineSeparator(), StandardCharsets.UTF_8.name())
                }
    }
}
