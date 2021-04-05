package app.models.entity;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TaskRequest {

    private String description;
    private String title;
    private boolean isCompleted;
    private Date dueTime;
    
}
