package model.sheets;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.UUID;

@RequiredArgsConstructor
@Accessors(fluent = true) @Getter @Setter
public class ImputationInformation {
    private @NonNull String id;

    private String teamName;

    private String spreadsheetId;

    private String timestamp;

    private String developerName;

    private String sprintName;

    private Double totalHours;

    private Double reHours;

    private Double rfHours;

    private Double cpHours;

    private Double fHours;

    private Double desHours;

    private Double gpHours;

    private Double docHours;

    private Double presHours;
}
