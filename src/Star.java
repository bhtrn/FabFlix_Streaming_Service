public class Star {
    private String starId;

    private String name;

    private String birthYear;

    public Star(){
        this.starId = "";
        this.name = "";
        this.birthYear = "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBirthYear(String birthYear) {
        this.birthYear = birthYear;
    }

    public void setStarId(String starId) {
        this.starId = starId;
    }

    public String getName() {
        return name;
    }

    public String getBirthYear() {
        return birthYear;
    }

    public String getStarId() {
        return starId;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Star Details - ");
        sb.append("Name:" + getName());
        sb.append(", ");
//        sb.append("ID:" + getStarId());
//        sb.append(", ");
        sb.append("Year:" + getBirthYear());
        sb.append(".");

        return sb.toString();
    }
}
