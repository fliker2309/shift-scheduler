# Implementation Plan - Shift Pattern Entity and Code Viewer UI

The goal is to implement the `ShiftPatternEntity` as shown in the screenshot and create a Jetpack Compose component that replicates the visual design of the code snippet viewer shown in the screenshot.

## Proposed Changes

### [Data Layer]
#### [MODIFY] [ShiftPatternEntity.kt](file:///E:/Android Sandbox/shiftscheduler/app/src/main/java/com/fliker/shiftscheduler/data/local/entity/ShiftPatternEntity.kt)
Update the entity to match the fields and annotations shown in the screenshot.

### [UI Layer]
#### [NEW] [CodeSnippetCard.kt](file:///E:/Android Sandbox/shiftscheduler/app/src/main/java/com/fliker/shiftscheduler/ui/components/CodeSnippetCard.kt)
Create a new Composable that replicates the visual design of the code snippet in the screenshot, including the dark theme, header with "Kotlin" text, and action icons.

#### [NEW] [ic_download.xml](file:///E:/Android Sandbox/shiftscheduler/app/src/main/res/drawable/ic_download.xml)
Vector drawable for the download icon.

#### [NEW] [ic_copy.xml](file:///E:/Android Sandbox/shiftscheduler/app/src/main/res/drawable/ic_copy.xml)
Vector drawable for the copy icon.

## Verification Plan
- Use `render_compose_preview` to verify the `CodeSnippetCard` looks like the screenshot.
- Run `gradle_build` to ensure the project compiles with the new entity.
