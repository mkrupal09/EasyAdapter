package easyadapter.dc.com.easyadapter;

/**
 * Created by HB on 11/7/18.
 */
public class Region {

    public String id;
    public String name;
    public String parentId;

    public static Region createDummy(String id, String name, String parentId) {
        Region region = new Region();
        region.id = id;
        region.name = name;
        region.parentId = parentId;
        return region;
    }
}
