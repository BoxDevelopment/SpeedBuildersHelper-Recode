package box.com.speedbuilderhelper;

public class BuildEntry {
    public String name;
    public String difficulty;
    public String variant;
    public double time;

    public BuildEntry(String name, String difficulty, String variant, double time) {
        this.name = name;
        this.difficulty = difficulty;
        this.variant = variant;
        this.time = time;
    }
}
