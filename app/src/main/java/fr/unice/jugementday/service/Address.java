package fr.unice.jugementday.service;

public class Address {

    private static final String url = "http://10.3.122.146/";
    private static String updatePage = "updatedata.php";
    private static String deletePage = "deletedata.php";
    private static String importPage = "importdata.php";
    private static String getPage = "getdata.php";
    private static String getPhotoPage = "getphoto.php";
    private static String importPhotoPage = "importphoto.php";

    private static String getPagePASSID = "?passid=Aij84k_-2RRS6d51dq6FSd698-(_45";
    private static String updatePagePASSID = "?passid=sdj-fK_OJF74AZsdQs6--_9js_S41-D";
    private static String deletePagePASSID = "?passid=sdfjJSDF_-ML3K42_--_12_-Skfl9";
    private static String importPagePASSID = "?passid=94JD-kd_us8-UwU-13-jws-_(";
    private static String getPhotoPagePASSID = "?passid=SUD24k_7DjQufy8137slsjQ809";
    private static String importPhotoPagePASSID = "?passid=12D-Jsk-DQl_qs8DQS--_3J2__";

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
