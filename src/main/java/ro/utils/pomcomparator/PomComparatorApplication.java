package ro.utils.pomcomparator;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        List<Dependency> oldDep = parseDependencyFile(oldPom);
        List<Dependency> newDep = parseDependencyFile(newPom);
        System.out.println("Total Dep OLD:" + oldDep.size());
        System.out.println("Total Dep NEW:" + newDep.size());
        List<Dependency> intersection = new ArrayList<>(newDep);
        intersection.retainAll(oldDep);

        List<Dependency> inOldNotInNew = oldDep.stream().filter(el -> !intersection.contains(el)).toList();
        List<Dependency> inNewNotInOld = newDep.stream().filter(el -> !intersection.contains(el)).toList();

        System.out.println("-------inOldNotInNew-------");
        inOldNotInNew.forEach(System.out::println);
        System.out.println("-------inNewNotInOld-------");
        inNewNotInOld.forEach(System.out::println);

		List<Dependency> diffVersions = new ArrayList<>(inOldNotInNew.stream()
				.map(dep->new Dependency(dep.getGroupId(), dep.getArtifactId(), null,null,null))
                .toList());
		diffVersions.retainAll(inNewNotInOld.stream()
				.map(dep->new Dependency(dep.getGroupId(), dep.getArtifactId(), null,null,null))
				.toList());
		System.out.println("-------Difference Versions-------");
		diffVersions.forEach(diff->{
			var oldVersion = inOldNotInNew.stream()
                    .filter(el->el.getGroupId().equals(diff.getGroupId())&&el.getArtifactId().equals(diff.getArtifactId()))
                    .findFirst()
                    .get()
                    .getVersion();
			var newVersion = inNewNotInOld.stream()
                    .filter(el->el.getGroupId().equals(diff.getGroupId())&&el.getArtifactId().equals(diff.getArtifactId()))
                    .findFirst()
                    .get()
                    .getVersion();

            int dotCount = 55 - (diff.getGroupId() + ":" + diff.getArtifactId()).length();
            String dots = IntStream.range(0, dotCount)
                    .mapToObj(i -> ".")
                    .reduce("", (a, b) -> a + b);;

			String template = String.format("%s %s %-10s -> %s", diff.getGroupId() + ":" + diff.getArtifactId(), dots, oldVersion, newVersion);
			System.out.println(template);
		});

    }

    public List<Dependency> parseDependencyFile(Resource resource) {
        try (Stream<String> stream = Files.lines(Paths.get(resource.getURI()))) {
            return stream.map(line -> {
                        var pt = Pattern.compile("([\\w\\.\\-]+):([\\w\\.\\-]+):(\\w+).*:([\\w+].*):([\\w\\.]+)");
                        Matcher m = pt.matcher(line);
                        System.out.println(line);
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
