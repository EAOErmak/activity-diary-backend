package com.example.activity_diary;

import com.example.activity_diary.platform.desktop.DesktopPlatformConfiguration;
import com.example.activity_diary.platform.web.WebPlatformConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(
        basePackages = "com.example.activity_diary",
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.REGEX,
                        pattern = "com\\.example\\.activity_diary\\.platform\\.web\\..*"
                ),
                @ComponentScan.Filter(
                        type = FilterType.REGEX,
                        pattern = "com\\.example\\.activity_diary\\.platform\\.desktop\\..*"
                )
        }
)
@Import({WebPlatformConfiguration.class, DesktopPlatformConfiguration.class})
@EnableScheduling
public class DiaryApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiaryApplication.class, args);
	}

}
