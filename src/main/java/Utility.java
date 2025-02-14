import org.graalvm.polyglot.Value;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Utility {

    static File classesFolder = new File("src/main/java/testSourceCode");

    static File jsonSavePath = new File("src/main/resources/testCases.json");

    public static void main(String[] args) {

        String[] classNames = {
                "org.graalvm.polyglot.Context",
                "org.graalvm.polyglot.Value",
                "org.graalvm.polyglot.Engine"
        };

        List<Class<?>> classes =  getTestClasses(classesFolder);

        convertClassesToJson(classes, jsonSavePath);

        //findMethodsInClasses(classNames);
    }

    private static void findMethodsInClasses(String[] classNames) {

        for (String className : classNames) {
            try {
                Class<?> requiredClass = Class.forName(className);
                findMethods(requiredClass);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    private static void findMethods(Class<?> requiredClass) {
        for (Method method : requiredClass.getDeclaredMethods()) {
            if (Value.class.isAssignableFrom(method.getReturnType())) {
                System.out.println(method.getName());
            }
            for (Parameter param : method.getParameters()) {
                if (Value.class.isAssignableFrom(param.getType())) {
                    System.out.println(method.getName());
                    break;
                }
            }
        }
    }


    private static List<Class<?>> getTestClasses(File folder) {
        List<Class<?>> classes = new ArrayList<>();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".java"));

        if (files != null) {
            for (File file : files) {
                try {
                    String className = extractClassName(file);
                    Class<?> classObject = Class.forName(className);
                    classes.add(classObject);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return classes;
    }

    private static String extractClassName(File file) {
        String fileName = file.getName().replace(".java", "");
        return "testSourceCode." + fileName;
    }



    private static void convertClassesToJson(List<Class<?>> classes, File jsonSavePath) {
        JSONObject rootNode = new JSONObject();

        for (Class<?> classObject : classes) {
            JSONObject classNode = new JSONObject();
            Method[] methods = classObject.getDeclaredMethods();

            for (Method method : methods) {
                try {
                    List<String> lines = getLinesOfCode(method);
                    classNode.put(method.getName(), lines);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            rootNode.put(classObject.getSimpleName(), classNode);
        }

        try (FileWriter fileWriter = new FileWriter(jsonSavePath)) {
            fileWriter.write(rootNode.toString(4));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static List<String> getLinesOfCode(Method method) throws IOException {
        List<String> result = new ArrayList<>();
        File sourceFile = new File(classesFolder.getAbsolutePath() + "/"
                + method.getDeclaringClass().getSimpleName() + ".java");
        List<String> lines = Files.readAllLines(Paths.get(sourceFile.getPath()));

        String methodSignature = method.getName() + "(";
        boolean insideMethod = false;
        int nestedLevel = 0;

        for (String line : lines) {
            if (line.contains("{")) {
                nestedLevel++;

                if (line.contains(methodSignature) &&
                        (line.trim().startsWith("public") || line.trim().startsWith("private"))) {
                    insideMethod = true;
                    nestedLevel = 1;
                    continue;
                }
            }
            if (insideMethod) {
                result.add(line);

            }
            if (line.contains("}")) {

                int nestedAccumulation = 0;
                String tempString = line;
                while (tempString.contains("}")) {
                    nestedAccumulation++;
                    tempString = tempString.replaceFirst("}",
                            "");
                }
                nestedLevel -= nestedAccumulation;
                if (nestedLevel == 0) {
                    break;
                }
            }

        }
        return result;
    }
}
