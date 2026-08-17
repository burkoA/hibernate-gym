# Repository review: Trainer, Trainee, Training

## 1) Main finding
This project does **not** contain custom in-memory repositories for `Trainer`, `Trainee`, or `Training`.

Instead, it uses **Spring Data JPA / Hibernate** repositories:
- `src/main/java/epam/arsen/burko/gym/repository/TrainerRepository.java`
- `src/main/java/epam/arsen/burko/gym/repository/TraineeRepository.java`
- `src/main/java/epam/arsen/burko/gym/repository/TrainingRepository.java`

So the primary storage is the database, while Hibernate may keep entities in the transaction/session persistence context temporarily.

## 2) What objects are stored

### `TrainerRepository`
Stores `Trainer` entities (`JpaRepository<Trainer, Long>`).

Important referenced objects inside `Trainer`:
- `specialization` → `TrainingType` (`@ManyToOne`)
- `trainings` → `List<Training>` (`@OneToMany(mappedBy = "trainer")`)
- `trainees` → `Set<Trainee>` (`@ManyToMany(mappedBy = "trainers")`)

### `TraineeRepository`
Stores `Trainee` entities (`JpaRepository<Trainee, Long>`).

Important referenced objects inside `Trainee`:
- `trainings` → `List<Training>` (`@OneToMany(mappedBy = "trainee", cascade = CascadeType.REMOVE)`)
- `trainers` → `Set<Trainer>` (`@ManyToMany` through join table `trainee_trainer`)

### `TrainingRepository`
Stores `Training` entities (`JpaRepository<Training, Long>`).

Important referenced objects inside `Training`:
- `trainee` → `Trainee` (`@ManyToOne`)
- `trainer` → `Trainer` (`@ManyToOne`)
- `trainingType` → `TrainingType` (`@ManyToOne`)

## 3) How objects are referenced
- `Trainer` and `Trainee` both inherit from `User`, so each also has `id`, `firstName`, `lastName`, `username`, `password`, `isActive`.
- `Training` links one trainee to one trainer and one training type.
- The trainee-trainer assignment is stored on the **owning side** in `Trainee.trainers`; `Trainer.trainees` is the inverse side.
- `Training` is the owning side of both `Training -> Trainee` and `Training -> Trainer` relations.

## 4) How objects are removed

### Trainee removal
Explicit delete flow:
- `TraineeService.deleteTrainee(String username)`
- it loads the trainee by username;
- removes the trainee from already-loaded `Trainer.trainees` collections in memory;
- clears `Trainee.trainers`;
- calls `traineeRepository.delete(trainee)`.

Effects of deleting a trainee:
1. the `Trainee` entity is removed;
2. its `Training` entities are also removed because of `cascade = CascadeType.REMOVE` on `Trainee.trainings`;
3. trainee-trainer join table rows are removed from the owning `Trainee` side.

### Trainer removal
Explicit delete flow:
- `TrainerService.deleteTrainer(String username)`
- it loads the trainer by username;
- removes the trainer's `Training` records using `trainingRepository.deleteAll(...)`;
- removes the trainer from each linked trainee's `trainers` set;
- clears the loaded `trainer.trainings` and `trainer.trainees` collections in memory;
- calls `trainerRepository.delete(trainer)`.

Effects of deleting a trainer:
1. the `Trainer` entity is removed;
2. related `Training` rows are explicitly deleted first, so there are no orphaned records;
3. trainee-trainer join table links are removed from the owning `Trainee` side.

### Training removal
Explicit delete flow:
- `TrainingService.deleteTraining(Long id)`
- it loads the training by id;
- removes the training from already-loaded `Trainee.trainings` and `Trainer.trainings` collections in memory;
- calls `trainingRepository.delete(training)`.

Effects of deleting a training:
1. the `Training` entity is removed;
2. loaded parent collections are kept synchronized for the current persistence context.

## 5) Potential memory leaks / retention issues

## No classic in-memory repository leak
Because these repositories are JPA interfaces, there is **no long-lived application-managed `Map`/`List` repository storage** that keeps growing after deletions. So the classic “object deleted logically but still left inside an in-memory repository collection” problem is **not present here**.

## Potential stale in-memory references in entity graphs
There are still a few **object graph consistency** risks inside the current Hibernate session:

### A. `TraineeService.updateTrainers(...)`
This method replaces `trainee.trainers` and saves the trainee, but it does **not** also update the inverse collection `trainer.trainees` in Java memory.

Impact:
- database state should be updated from the owning side;
- already-loaded `Trainer` objects in the same persistence context may still hold stale `trainees` collections until reloaded/refreshed.

This is **not a permanent memory leak**, but it can retain outdated references for the lifetime of the current session/transaction.

### B. `TrainingService.add(...)` and `addTraining(...)`
These methods create and save a `Training`, but they do **not** add the new training to `trainer.trainings` or `trainee.trainings` collections in Java memory.

Impact:
- database state is correct because `Training` owns those relations;
- already-loaded `Trainer` / `Trainee` entities may expose outdated `trainings` collections until reloaded.

This does not leak memory permanently, but it means the in-memory object graph is not fully synchronized.

## 6) Risk summary
- **Confirmed:** no custom in-memory repositories, so no repository-collection leak.
- **Confirmed:** trainee deletion cascades to `Training`.
- **Confirmed:** explicit delete flows now exist for trainee, trainer, and training.
- **Confirmed:** trainer deletion now explicitly removes related trainings before the trainer itself is deleted.
- **Potential issue:** bidirectional relations are not maintained on both sides in Java memory, which can leave temporary stale references in the active Hibernate session.
- **Remaining issue:** `updateTrainers(...)` and training creation still do not synchronize both sides of all bidirectional collections in Java memory.

## 7) Parameter passing in Java
Java always passes arguments **by value**.

- For **primitives** (`int`, `long`, `boolean`, etc.), the copied value is the primitive itself. Changing the parameter inside a method does not change the caller's variable.
- For **objects**, the copied value is the **reference** to the object, not the object itself. That means a method can mutate the same object instance through the copied reference, but it cannot replace the caller's reference by assigning the parameter to a new object.

Applied to this project:
- when an entity such as `Trainee` or `Trainer` is passed into a method, code can modify its fields or collections;
- but assigning the local parameter to a new entity instance does not change the original caller's variable.

This matters for collection-backed associations: mutating `trainee.getTrainers()` affects the same underlying entity object, while reassigning only a local variable does not.

## 8) Collection choice: memory usage and performance
The collection types used in the entity model affect both lookup behavior and memory overhead.

### `ArrayList` / `List`
Used conceptually for ordered, index-based collections such as `trainings`.

Characteristics:
- **Fast iteration** and **fast append** in most cases;
- **O(1)** positional access by index;
- removing from the middle usually shifts elements, so deletions are more expensive;
- typically lower per-element overhead than hash-based collections.

In this application, a list is reasonable for `Training` collections because order can matter and duplicates may be acceptable depending on business rules. The trade-off is that duplicate detection or membership checks are slower than in a set.

### `HashSet` / `Set`
Used for `Trainee.trainers` and `Trainer.trainees`.

Characteristics:
- efficient membership checks and insert/remove on average (**O(1)**);
- prevents duplicates naturally;
- uses more memory than a list because hashing requires bucket/table structures in addition to stored elements.

In this application, `HashSet` is a good fit for trainer-trainee relationships because duplicate assignments should not exist. The memory cost is higher than a list, but it helps preserve uniqueness and makes add/remove checks cheaper.

### `HashMap`
Used in `TraineeService.updateTrainers(...)` as `trainersByUsername`.

Characteristics:
- average **O(1)** key lookup;
- extra memory overhead for buckets, entries, keys, values, and resize slack;
- very useful when repeated lookup by key is needed.

In this application, building a `HashMap<String, Trainer>` avoids repeated linear scans when resolving requested trainer usernames. That improves performance for larger trainer lists at the cost of temporary extra memory during the method call.

## 9) Practical conclusion
For this codebase, the main memory/performance trade-offs are not about custom in-memory repositories, but about:
- temporary Hibernate-managed entity retention within a session;
- choosing `Set` for uniqueness in many-to-many relationships;
- using `List` where ordered related records are expected;
- using `HashMap` in service logic to trade a small amount of temporary memory for faster lookups.

Overall, the current collection choices are reasonable for the domain model. The larger remaining correctness concern is keeping both sides of bidirectional associations synchronized in memory when entities are added or replaced.

