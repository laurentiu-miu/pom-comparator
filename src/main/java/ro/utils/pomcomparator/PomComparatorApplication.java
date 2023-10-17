package ro.utils.pomcomparator;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SpringBootApplication
@Log4j2
public class PomComparatorApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(PomComparatorApplication.class, args);
    }

    @Value("classpath:new.pom.txt")
    private Resource newPom;
    @Value("classpath:old.pom.txt")
    private Resource oldPom;

    @Override
    public void run(String... args) throws Exception {
        List<String> output = new ArrayList<>();

        List<Dependency> oldDep = parseDependencyFile(oldPom);
        List<Dependency> newDep = parseDependencyFile(newPom);
        output.add("Total Dep OLD:" + oldDep.size());
        output.add("Total Dep NEW:" + newDep.size());
        List<Dependency> intersection = new ArrayList<>(newDep);
        intersection.retainAll(oldDep);

        List<Dependency> inOldNotInNew = difference(oldDep,intersection);
        List<Dependency> inNewNotInOld = difference(newDep,intersection);

        List<Dependency> commonLibrariesDifferentVersions = new ArrayList<>(onlyGroupIdAndArtifactId(inOldNotInNew));
        commonLibrariesDifferentVersions.retainAll(onlyGroupIdAndArtifactId(inNewNotInOld));
        output.add("-------Difference Versions-----");
        commonLibrariesDifferentVersions.forEach(dep->{
            var oldVersion = getVersionForDiff(inOldNotInNew, dep);
            var newVersion = getVersionForDiff(inNewNotInOld, dep);
            output.add(printDependencyDifference(dep,oldVersion,newVersion));
        });

        output.add("-------In Old Not In New-------");
        inOldNotInNew.forEach(e->output.add(e.toString()));
        output.add("-------In New Not In Old-------");
        inNewNotInOld.forEach(e->output.add(e.toString()));

        writeLinesToFile(generateFileNameWithDateTime(),output);
    }

    public static String generateFileNameWithDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        return LocalDateTime.now().format(formatter) + ".txt";
    }
    public static void writeLinesToFile(String filePath, List<String> lines) {
        try {
            Path path = Paths.get(filePath);
            Files.write(path, lines);
        } catch (Exception e) {
            log.error(e);
        }
    }
    private List<Dependency> difference(List<Dependency> list1, List<Dependency> list2){
        return list1.stream().filter(el -> !list2.contains(el)).toList();
    }
    private List<Dependency> onlyGroupIdAndArtifactId(List<Dependency> list){
        return list.stream()
                .map(dep->new Dependency(dep.getGroupId(), dep.getArtifactId(), null,null,null))
                .toList();
    }

    private String printDependencyDifference(Dependency dependency, String oldVersion, String newVersion){
        int dotCount = 55 - (dependency.getGroupId() + ":" + dependency.getArtifactId()).length();
        String dots = IntStream.range(0, dotCount)
                .mapToObj(i -> ".")
                .reduce("", (a, b) -> a + b);;
        return String.format("%s %s %-10s -> %s", dependency.getGroupId() + ":" + dependency.getArtifactId(), dots, oldVersion, newVersion);
    }

    private String getVersionForDiff(List<Dependency> elements, Dependency dependency) {
        return elements.stream()
                .filter(el -> el.getGroupId().equals(dependency.getGroupId()) && el.getArtifactId().equals(dependency.getArtifactId()))
                .findFirst()
                .get()
                .getVersion();
    }

    public List<Dependency> parseDependencyFile(Resource resource) {
        try (Stream<String> stream = Files.lines(Paths.get(resource.getURI()))) {
            return stream.map(line -> {
                        var pt = Pattern.compile("([\\w\\.\\-]+):([\\w\\.\\-]+):(\\w+).*:([\\w+].*):([\\w\\.]+)");
                        Matcher m = pt.matcher(line);
                        if (m.find()) {
                            return new Dependency(m.group(1), m.group(2), m.group(3), m.group(4), m.group(5));
                        } else {
                            return null;
                        }
                    }).filter(Objects::nonNull)
                    .toList();
        } catch (IOException e) {
            log.error(e);
        }
        throw new RuntimeException("No dependency found in file!");
    }
}
