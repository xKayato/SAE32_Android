package fr.unice.jugementday.service;

public class Address {

    private static String url = "http://10.3.122.146/";
    private static String updatePage = "updatedata.php";
    private static String deletePage = "deletedata.php";
    private static String importPage = "importdata.php";
    private static String getPage = "getdata.php";
    private static String getPhotoPage = "getphoto.php";
    private static String importPhotoPage = "importphoto.php";

    private static String getPagePASSID = "?passid=SalutJeSuisUnMotDePassePourGet";
    private static String updatePagePASSID = "?passid=SalutJeSuisUnMotDePassePourUpdate";
    private static String deletePagePASSID = "?passid=SalutJeSuisUnMotDePassePourDelete";
    private static String importPagePASSID = "?passid=SalutJeSuisUnMotDePassePourImport";
    private static String getPhotoPagePASSID = "?passid=SalutJeSuisUnMotDePassePourGetPhoto";
    private static String importPhotoPagePASSID = "?passid=SalutJeSuisUnMotDePassePourImportPhoto";

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
