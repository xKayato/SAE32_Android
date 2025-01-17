package fr.unice.jugementday.service;

public class Address {

    private static final String url = "http://10.3.122.146/";
    private static final String updatePage = "updatedata.php";
    private static final String deletePage = "deletedata.php";
    private static final String importPage = "importdata.php";
    private static final String getPage = "getdata.php";
    private static final String getPhotoPage = "getphoto.php";
    private static final String importPhotoPage = "importphoto.php";

    private static final String getPagePASSID = "?passid=Aij84k_-2RRS6d51dq6FSd698-(_45";
    private static final String updatePagePASSID = "?passid=sdj-fK_OJF74AZsdQs6--_9js_S41-D";
    private static final String deletePagePASSID = "?passid=sdfjJSDF_-ML3K42_--_12_-Skfl9";
    private static final String importPagePASSID = "?passid=94JD-kd_us8-UwU-13-jws-_(";
    private static final String getPhotoPagePASSID = "?passid=SUD24k_7DjQufy8137slsjQ809";
    private static final String importPhotoPagePASSID = "?passid=12D-Jsk-DQl_qs8DQS--_3J2__";

    public static String getUrl() {
        return url;
    }

    public static String getUpdatePage() {
        return url + updatePage + updatePagePASSID;
    }

    public static String getDeletePage() {
        return url + deletePage + deletePagePASSID;
    }

    public static String getImportPage() {
        return url + importPage + importPagePASSID;
    }

    public static String getGetPage() {
        return url + getPage + getPagePASSID;
    }

    public static String getGetPhotoPage() {
        return url + getPhotoPage + getPhotoPagePASSID;
    }

    public static String getImportPhotoPage() {
        return url + importPhotoPage + importPhotoPagePASSID;
    }





}
