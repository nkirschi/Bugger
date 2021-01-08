package tech.bugger.global.transfer;

public final class TopReport {

    private int id;

    private String title;

    private String creator;

    private int relevanceGain;

    public TopReport(int id, String title, String creator, int relevanceGain) {
        this.id = id;
        this.title = title;
        this.creator = creator;
        this.relevanceGain = relevanceGain;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCreator() {
        return creator;
    }

    public int getRelevanceGain() {
        return relevanceGain;
    }

    @Override
    public String toString() {
        return "TopReport{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", creator='" + creator + '\'' +
                ", relevanceGain=" + relevanceGain +
                '}';
    }

}
