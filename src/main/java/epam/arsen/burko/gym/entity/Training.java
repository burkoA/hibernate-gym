package epam.arsen.burko.gym.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "training")
@Getter
@Setter
@NoArgsConstructor
public class Training {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "trainee_id")
    private Trainee trainee;

    @ManyToOne(optional = false)
    @JoinColumn(name = "trainer_id")
    private Trainer trainer;

    @Column(nullable = false)
    private String trainingName;

    @ManyToOne(optional = false)
    @JoinColumn(name = "training_type_id")
    private TrainingType trainingType;

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date trainingDate;

    @Column(nullable = false)
    private Integer trainingDuration;
}
