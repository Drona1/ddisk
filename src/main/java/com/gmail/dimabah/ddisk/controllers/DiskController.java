package com.gmail.dimabah.ddisk.controllers;

import com.gmail.dimabah.ddisk.dto.*;
import com.gmail.dimabah.ddisk.models.DiskFile;
import com.gmail.dimabah.ddisk.models.DiskFolder;
import com.gmail.dimabah.ddisk.models.DiskObject;
import com.gmail.dimabah.ddisk.models.DiskUser;
import com.gmail.dimabah.ddisk.models.enums.AccessRights;
import com.gmail.dimabah.ddisk.models.enums.UserRole;
import com.gmail.dimabah.ddisk.services.DiskFileService;
import com.gmail.dimabah.ddisk.services.DiskFolderService;
import com.gmail.dimabah.ddisk.services.DiskObjectService;
import com.gmail.dimabah.ddisk.services.DiskUserService;
import com.google.cloud.storage.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
public class DiskController {
    private final DiskUserService userService;
    private final PasswordEncoder encoder;
    private final DiskFolderService folderService;

    private final DiskFileService fileService;
    private final DiskObjectService objectService;

    @Value("${server.domain}")
    private String domain;

    public DiskController(DiskUserService userService, PasswordEncoder encoder,
                          DiskFolderService folderService, DiskFileService fileService,
                          DiskObjectService objectService) {
        this.userService = userService;
        this.encoder = encoder;
        this.folderService = folderService;
        this.fileService = fileService;
        this.objectService = objectService;
    }

    @GetMapping("/")
    public String index(Model model) {
        DiskUser dUser = getCurrentDiskUser();

        if (dUser == null) {
            return "unauthorized";
        }

        model.addAttribute("email", dUser.getEmail());
        DiskFolderDTO folder = dUser.getMainFolder().toFolderDTO(true);
        folder.setDomain(domain);
        model.addAttribute("folder", folder);

        return "index";
    }

    @GetMapping("/shared")
    public String sharedWithMe(Model model) {
        DiskUser dUser = getCurrentDiskUser();

        if (dUser == null) {
            return "unauthorized";
        }

        model.addAttribute("email", dUser.getEmail());

        DiskFolderDTO folder = sharedObjToDiskFolderDTO(dUser.getSharedObjects());
        folder.setDomain(domain);
        model.addAttribute("folder", folder);

        return "index";
    }

    private DiskFolderDTO sharedObjToDiskFolderDTO(List<DiskObject> objects) {
        DiskFolderDTO result = new DiskFolderDTO();
        List<DiskObjectDTO> folders = new ArrayList<>();
        List<DiskFileDTO> files = new ArrayList<>();
        for (var obj : objects) {
            if (obj instanceof DiskFolder) {
                if (obj.getLive()) {
                    folders.add(obj.toDTO());
                }
            }
            if (obj instanceof DiskFile) {
                if (obj.getLive()) {
                    files.add(((DiskFile) obj).toDTO());
                }
            }
        }
        result.setFolders(folders);
        result.setFiles(files);

        result.setAddress("shared");

        return result;
    }

    @GetMapping("files/{address}")
    @PreAuthorize("hasPermission(#address, 'VIEWER')")
    public String files(@PathVariable("address") String address,
                        @RequestParam(required = false) String link,
                        Model model) {

        DiskFile file = fileService.findByAddress(address);
        DiskUser dUser = getCurrentDiskUser();
        String email = getEmail(dUser);
        model.addAttribute("email", email);

        if (file != null) {
            if (!email.equals("anonymousUser") && "shared".equals(link)) {
                addUserToShared(file, dUser);
            }

            OneFileDTO oneFileDTO = file.oneFileDTO();
            oneFileDTO.setDomain(domain);
            model.addAttribute("folder", oneFileDTO);
        }

        return "index";
    }

    private String getEmail(DiskUser dUser) {
        if (dUser == null) {
            return "anonymousUser";
        }
        return dUser.getEmail();
    }

    private void addUserToShared(DiskObject object, DiskUser user) {
        object.addUserToShared(user);
        objectService.updateObj(object);
    }


    @GetMapping("folders/{address}")
    @PreAuthorize("hasPermission(#address, 'VIEWER')")
    public String folders(@PathVariable("address") String address,
                          @RequestParam(required = false) String link,
                          Model model) {

        DiskFolder folder = folderService.findByAddress(address);
        DiskUser dUser = getCurrentDiskUser();
        String email = getEmail(dUser);
        model.addAttribute("email", email);

        if (folder != null) {
            if (!email.equals("anonymousUser") && "shared".equals(link)) {
                addUserToShared(folder, dUser);
            }

            DiskFolderDTO folderDTO = folder.toFolderDTO(true);
            folderDTO.setDomain(domain);
            model.addAttribute("folder", folderDTO);
        }

        return "index";
    }

    @GetMapping("/bin")
    public String bin(Model model) {
        DiskUser dUser = getCurrentDiskUser();

        if (dUser == null) {
            return "unauthorized";
        }
        model.addAttribute("email", dUser.getEmail());
        model.addAttribute("bin", dUser.getBin().toDTO());

        return "bin";
    }


    @PostMapping(value = "/newuser")
    public String update(@RequestParam String email,
                         @RequestParam String pass,
                         Model model) {
        String passHash = encoder.encode(pass);

        if (!userService.addUser(email, passHash, UserRole.USER)) {
            model.addAttribute("exists", true);
            model.addAttribute("email", email);
            return "register";
        }
        return "redirect:/";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/unauthorized")
    public String unauthorized(Model model) {
        User user = getCurrentUser();

        String email;

        if (user == null) {
            email = "anonymousUser";
        } else {
            email = user.getUsername();
        }
        model.addAttribute("email", email);
        return "unauthorized";
    }

    @PostMapping(value = "/upload")
    public String upload(@RequestParam(required = false) String newFolder,
                         @RequestParam(required = false) String currentFolder,
                         @RequestParam(required = false) String[] webkitRelativePaths,
                         @RequestParam(required = false) MultipartFile[] files) {
        if (currentFolder == null || "file".equals(currentFolder) || "shared".equals(currentFolder)) {
            return "redirect:/";
        }

        DiskUser dUser = getCurrentDiskUser();
        DiskFolder dFolder = folderService.findByAddress(currentFolder);

        if (dUser == null) {
            return "unauthorized";
        }
        if (dFolder == null) {
            return "redirect:/";
        }
        if (newFolder != null) {
            folderService.createFolder(dUser, newFolder, dFolder);
        }

        if (files != null) {
            if (webkitRelativePaths != null) {
                uploadDir(files, webkitRelativePaths, dUser, dFolder);
            } else {
                for (var file : files) {
                    uploadFile(file, dUser, dFolder);
                }
            }
        }

        return "redirect:/folders/" + currentFolder;
    }

    @PostMapping(value = "/rename")
    public String rename(@RequestParam String address,
                         @RequestParam String newName,
                         @RequestParam String currentFolder) {
        DiskUser dUser = getCurrentDiskUser();

        objectService.rename(address, newName, dUser);
        return "redirect:/folders/" + currentFolder;
    }

    @PostMapping(value = "/download")
    public void download(@RequestParam(value = "selectedObjects[]", required = false) String[] addressList,
                         HttpServletResponse response) throws IOException {

        DiskUser dUser = getCurrentDiskUser();

        if (addressList != null && addressList.length > 0) {
//       Not used because there is no access to the server's local disk
//            List<FileToDownloadDTO> files = objectService.getFileListByAddress(Arrays.asList(addressList), dUser);

            List<BlobToDownloadDTO> blobs = objectService.getBlobListByAddress(Arrays.asList(addressList), dUser);
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"files.zip\"");

            List<String> filePath = getFilePathListForRemoteDrive(blobs);
            zipObjectsFromRemoteDrive(response, blobs, filePath);

        }
    }

    private void zipObjectsFromRemoteDrive(HttpServletResponse response,
                                           List<BlobToDownloadDTO> blobs, List<String> filePath) throws IOException {

        try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
            for (int i = 0; i < filePath.size(); i++) {
                if (!"*".equals(filePath.get(i))) {
                    ZipEntry zipEntry = new ZipEntry(filePath.get(i));
                    zipOut.putNextEntry(zipEntry);
                    blobs.get(i).getBlob().downloadTo(zipOut);
                    zipOut.closeEntry();
                }
            }
        }
    }

    // Not used because there is no access to the server's local disk
    private void zipObjectsFromLocalDrive(HttpServletResponse response,
                                          List<FileToDownloadDTO> files, List<String> filePath) throws IOException {
        try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
            for (int i = 0; i < filePath.size(); i++) {
                if (!"*".equals(filePath.get(i))) {
                    ZipEntry zipEntry = new ZipEntry(filePath.get(i));
                    zipOut.putNextEntry(zipEntry);
                    Files.copy(files.get(i).getFile().toPath(), zipOut);
                    zipOut.closeEntry();
                }
            }
        }
    }

    private List<String> getFilePathListForRemoteDrive(List<BlobToDownloadDTO> blobs) {
        List<String> filePath = new ArrayList<>();
        String folderName = "";
        for (var blob : blobs) {
            if (blob.getFilePath().startsWith("*")) {
                folderName = blob.getFilePath().substring(2) + "/";
                filePath.add("*");
            } else {
                String fileName = folderName + blob.getNewFileName();
                int counter = 1;

                while (filePath.contains(fileName)) {
                    int lastIndex = blob.getNewFileName().lastIndexOf('.');
                    if (lastIndex == -1) {
                        fileName = folderName + blob.getNewFileName() + " (" + counter++ + ")";
                    } else {
                        fileName = folderName
                                + blob.getNewFileName().substring(0, lastIndex)
                                + " (" + counter++ + ")"
                                + blob.getNewFileName().substring(lastIndex);
                    }
                }

                filePath.add(fileName);
            }
        }
        return filePath;
    }

    // Not used because there is no access to the server's local disk
    private List<String> getFilePathList(List<FileToDownloadDTO> files) {
        List<String> filePath = new ArrayList<>();
        String folderName = "";
        for (var file : files) {
            if (file.getFile().getPath().startsWith("*")) {
                folderName = file.getFile().getPath().substring(2) + "/";
                filePath.add("*");
            } else {
                String fileName = folderName + file.getNewFileName();
                int counter = 1;

                while (filePath.contains(fileName)) {
                    int lastIndex = file.getNewFileName().lastIndexOf('.');
                    if (lastIndex == -1) {
                        fileName = folderName + file.getNewFileName() + " (" + counter++ + ")";
                    } else {
                        fileName = folderName
                                + file.getNewFileName().substring(0, lastIndex)
                                + " (" + counter++ + ")"
                                + file.getNewFileName().substring(lastIndex);
                    }
                }

                filePath.add(fileName);
            }
        }
        return filePath;
    }


    @PostMapping("remove")
    public ResponseEntity<Void> removeObj(@RequestParam(value = "selectedObjects[]", required = false) String[]
                                                  addressList) {
        if (addressList != null && addressList.length > 0) {
            DiskUser dUser = getCurrentDiskUser();
            objectService.remove(Arrays.asList(addressList), dUser);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/delete")
    public ResponseEntity<Void> deleteObj(@RequestParam(value = "selectedObjects[]", required = false) String[]
                                                  addressList) {
        if (addressList != null && addressList.length > 0) {
            DiskUser dUser = getCurrentDiskUser();
            objectService.delete(Arrays.asList(addressList), dUser);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/restore")
    public ResponseEntity<Void> restoreObj(@RequestParam(value = "selectedObjects[]", required = false) String[]
                                                   addressList) {
        if (addressList != null && addressList.length > 0) {
            DiskUser dUser = getCurrentDiskUser();
            objectService.restore(Arrays.asList(addressList), dUser);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/share")
    public ResponseEntity<Void> share(@RequestParam(name = "users[]", required = false) String[] users,
                                      @RequestParam(name = "accessRights[]", required = false) String[] accessRights,
                                      @RequestParam(name = "globalAccessRight", required = false) String globalAccessRight,
                                      @RequestParam(name = "currentObj") String currentObj) {

        DiskUser dUser = getCurrentDiskUser();
        Map<DiskUser, AccessRights> map = null;
        if ("".equals(globalAccessRight)) {
            globalAccessRight = null;
        }
        if (users != null && users.length > 0) {
            map = userService.convertToMap(users, accessRights);
        }
        boolean result = objectService.share(map, globalAccessRight, currentObj, dUser);
        if (result) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
    }


    private User getCurrentUser() {
        Object object = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        if ("anonymousUser".equals(object)) {
            return null;
        }
        return (User) object;
    }

    private DiskUser getCurrentDiskUser() {
        User user = getCurrentUser();
        if (user == null) {
            return null;
        }
        String email = user.getUsername();
        return userService.findByEmail(email);
    }

    private void uploadDir(MultipartFile[] files, String[] webkitRelativePaths,
                           DiskUser user, DiskFolder parentFolder) {
        Map<String, DiskFolder> folderHashMap = new HashMap<>();
        DiskFolder tempFolder;

        for (int i = 0; i < webkitRelativePaths.length; i++) {
            tempFolder = parentFolder;
            if (webkitRelativePaths[i].indexOf('/') != -1) {
                String[] addressArr = webkitRelativePaths[i]
                        .substring(0, webkitRelativePaths[i].lastIndexOf('/')).split("/");
                StringBuilder sb = new StringBuilder();

                for (String s : addressArr) {
                    sb.append(s).append('/');
                    if (!folderHashMap.containsKey(sb.toString())) {
                        tempFolder = folderService.createFolder(user, s, tempFolder);
                        folderHashMap.put(sb.toString(), tempFolder);
                    } else {
                        tempFolder = folderHashMap.get(sb.toString());
                    }
                }
            }

            uploadFile(files[i], user, tempFolder);
        }
    }

    private void uploadFile(MultipartFile file, DiskUser user, DiskFolder parentFolder) {
        String fileName = file.getOriginalFilename();

        if (fileName == null) {
            return;
        }

        if (fileName.lastIndexOf('/') != -1) {
            fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
        }
        DiskFile dFile = fileService.createFile(user, fileName, file.getSize(), parentFolder);
        if (!"".equals(file.getOriginalFilename())) {
            String filePath = "ddisk/" + user.getEmail() + "/" + dFile.getAddress() + "/" + fileName;
            uploadFileToRemoteDrive(filePath, file);
        }
    }

    private void uploadFileToRemoteDrive(String objectName, MultipartFile file) {
        String projectId = "ddisk-diploma";
        String bucket = "ddisk-storage";

        Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
        BlobId blobId = BlobId.of(bucket, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

        Storage.BlobWriteOption precondition;
        if (storage.get(bucket, objectName) == null) {
            precondition = Storage.BlobWriteOption.doesNotExist();
        } else {
            precondition =
                    Storage.BlobWriteOption.generationMatch(
                            storage.get(bucket, objectName).getGeneration());
        }
        try {
            InputStream targetStream = new ByteArrayInputStream(file.getBytes());
            storage.createFrom(blobInfo, targetStream, precondition);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Not used because there is no access to the server's local disk
    private void saveFileToLocalDrive(DiskUser user, DiskFile dFile, MultipartFile file, String fileName) {
        try {
            String dirPath = "D:/upload_dir/" + user.getEmail() + "/" + dFile.getAddress() + "/";
            File dir = new File(dirPath);
            dir.mkdirs();
            file.transferTo(new File(dirPath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
