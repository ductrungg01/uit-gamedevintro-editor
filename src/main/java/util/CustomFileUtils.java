package util;

public class CustomFileUtils {
    private String _fileName = "";
    private String _fileAbsolutePath = "";
    private String _fileParentDirectory = "";

    public CustomFileUtils(String pathFile){
        setPath(pathFile);
    }

    public String fileName(){ return _fileName;}
    public String fileAbsolutePath() { return _fileAbsolutePath;}
    public String fileParentDirectory() { return _fileParentDirectory;}

    private void setPath(String path){
        _fileAbsolutePath = path;
        int lastSlashIndex = path.lastIndexOf("\\");

        if (lastSlashIndex == - 1) {
            _fileName = "";
            _fileParentDirectory = "";
            return;
        }

        _fileName = path.substring(lastSlashIndex + 1);
        _fileParentDirectory = path.substring(0, lastSlashIndex + 1);
    }
}
