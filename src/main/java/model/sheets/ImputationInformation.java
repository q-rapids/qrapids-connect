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

    private String totalHours;

    private String reHours;

    private String rfHours;

    private String cpHours;

    private String fHours;

    private String desHours;

    private String gpHours;

    private String docHours;

    private String presHours;
}
