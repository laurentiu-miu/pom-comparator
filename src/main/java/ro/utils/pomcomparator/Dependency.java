package ro.utils.pomcomparator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Dependency {
    private String groupId;
    private String artifactId;
    private String type;
    private String version;
    private String scope;

    @Override
    public String toString() {
        return groupId + ':' + artifactId + ':' + type + ':' + version + ':' + scope;
    }
}
