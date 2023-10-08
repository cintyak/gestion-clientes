package bo.com.bisa.gpgw.msaccount.web.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Properties;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class VersionController {

    @GetMapping("/app/version")
    public ResponseEntity<VersionRes> getVersion() {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("git.properties")) {
            Properties properties = new Properties();
            properties.load(is);
            return ResponseEntity.ok(
                VersionRes.builder()
                    .appVersion(properties.getProperty("app.version"))
                    .gitData(GitData.builder()
                        .gitBranch(properties.getProperty("git.branch", "no-data"))
                        .gitCommitId(properties.getProperty("git.commit.id", "no-data"))
                        .gitCommitTime(ZonedDateTime.parse(properties.getProperty("git.commit.time")))
                        .build())
                    .build()
            );

        } catch (IOException e) {
            log.error("git.properties file not found: {}", e.getMessage());
            return ResponseEntity.ok(
                VersionRes.builder()
                    .appVersion("1.0.0-devmode")
                    .build()
            );
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    static class VersionRes {
        public String appVersion;
        public GitData gitData;
    }

    @Data
    @Builder
    @AllArgsConstructor
    static class GitData {
        public String gitBranch;
        public String gitCommitId;
        private ZonedDateTime gitCommitTime;
    }
}
