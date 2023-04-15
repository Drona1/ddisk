package com.gmail.dimabah.ddisk.controllers;

import com.gmail.dimabah.ddisk.models.DiskFile;
import com.gmail.dimabah.ddisk.models.DiskFolder;
import com.gmail.dimabah.ddisk.models.DiskUser;
import com.gmail.dimabah.ddisk.models.enums.UserRole;
import com.gmail.dimabah.ddisk.services.DiskFileService;
import com.gmail.dimabah.ddisk.services.DiskFolderService;
import com.gmail.dimabah.ddisk.services.DiskObjectService;
import com.gmail.dimabah.ddisk.services.DiskUserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.ByteArrayResource;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
public class DiskController {
    private DiskUserService userService;
    private PasswordEncoder encoder;
    private DiskFolderService folderService;

    private DiskFileService fileService;
    private DiskObjectService objectService;

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
        model.addAttribute("folder", dUser.getMainFolder().toFolderDTO(true));

        return "index";
    }

    @GetMapping("folders/{address}")
    @PreAuthorize("hasPermission(#address, 'VIEWER')")
    public String folders(@PathVariable("address") String address, Model model) {

        DiskUser dUser = getCurrentDiskUser();

        if (dUser == null) {
            return "unauthorized";
        }

        DiskFolder folder = folderService.findByAddress(address);
        if (folder != null) {
            model.addAttribute("folder", folder.toFolderDTO(true));
        }
        model.addAttribute("email", dUser.getEmail());

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
        model.addAttribute("email", user.getUsername());
        return "unauthorized";
    }

    @PostMapping(value = "/upload")
    public String upload(@RequestParam(required = false) String newFolder,
                         @RequestParam(required = false) String currentFolder,
                         @RequestParam(required = false) String[] webkitRelativePaths,
                         @RequestParam(required = false) MultipartFile[] files) {
        if (currentFolder == null) {
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
    public String upload(@RequestParam String address,
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
            List<File> files = objectService.getFileListByAddress(Arrays.asList(addressList), dUser);

            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"files.zip\"");

            List<String> filePath = getFilePathList(files);

            try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
                for (int i = 0; i < filePath.size(); i++) {
                    if (!"*".equals(filePath.get(i))) {
                        ZipEntry zipEntry = new ZipEntry(filePath.get(i));
                        zipOut.putNextEntry(zipEntry);
                        Files.copy(files.get(i).toPath(), zipOut);
                        zipOut.closeEntry();
                    }
                }
            }
        }
    }
    private List<String> getFilePathList(List<File> files){
        List<String> filePath = new ArrayList<>();
        String folderName = "";
        for (var file : files) {
            if (file.getPath().startsWith("*")) {
                folderName = file.getPath().substring(2) + "/";
                filePath.add("*");
            } else {
                String fileName = folderName + file.getName();
                int counter = 1;

                while (filePath.contains(fileName)) {
                    int lastIndex = file.getName().lastIndexOf('.');
                    if (lastIndex == -1) {
                        fileName = folderName + file.getName() + "(" + counter++ + ")";
                    } else {
                        fileName = folderName
                                + file.getName().substring(0, lastIndex)
                                + "(" + counter++ + ")"
                                + file.getName().substring(lastIndex);
                    }
                }

                filePath.add(fileName);
            }
        }
        return filePath;
    }
//    private void addFilesToZip(ZipOutputStream zipOut, File file, String parentDir) throws IOException {
//        String entryName = parentDir + file.getName();
//        if (file.isDirectory()) {
//            if (!entryName.endsWith("/")) {
//                entryName += "/";
//            }
//            zipOut.putNextEntry(new ZipEntry(entryName));
//            zipOut.closeEntry();
//            File[] children = file.listFiles();
//            if (children != null) {
//                for (File childFile : children) {
//                    addFilesToZip(zipOut, childFile, entryName);
//                }
//            }
//        } else {
//            zipOut.putNextEntry(new ZipEntry(entryName));
//            Files.copy(file.toPath(), zipOut);
//            zipOut.closeEntry();
//        }
//    }


        @PostMapping("remove")
//    @PreAuthorize("hasPermission(#address, 'READER')")
//    public String removeObj(@RequestParam(required = false) String currentFolder,
//                            @RequestParam(required = false) Long[] addressList,
//                            Model model) {
//        User user = getCurrentUser();
//
//        String email = user.getUsername();
//        DiskUser dUser = userService.findByEmail(email);
//
//        if (dUser == null) {
//            return "unauthorized";
//        }
//
//        DiskFolder folder = folderService.findByAddress(currentFolder);
//        if (folder != null) {
//            model.addAttribute("folder", folder.toFolderDTO(true));
//        }
//        model.addAttribute("email", email);
//
//        return "redirect:/folders/" + currentFolder;
//    }
        public ResponseEntity<Void> removeObj (@RequestParam(value = "selectedObjects[]", required = false) String[]
        addressList){
            if (addressList != null && addressList.length > 0) {
                DiskUser dUser = getCurrentDiskUser();
                objectService.remove(Arrays.asList(addressList), dUser);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        }
        @PostMapping("/delete")
        public ResponseEntity<Void> deleteObj (@RequestParam(value = "selectedObjects[]", required = false) String[]
        addressList){
            if (addressList != null && addressList.length > 0) {
                DiskUser dUser = getCurrentDiskUser();
                objectService.delete(Arrays.asList(addressList),dUser);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        }
        @PostMapping("/restore")
        public ResponseEntity<Void> restoreObj (@RequestParam(value = "selectedObjects[]", required = false) String[]
        addressList){
            if (addressList != null && addressList.length > 0) {
                DiskUser dUser = getCurrentDiskUser();
                objectService.restore(Arrays.asList(addressList), dUser);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        }


        private User getCurrentUser () {
            return (User) SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getPrincipal();
        }
        private DiskUser getCurrentDiskUser(){
            User user = getCurrentUser();
            String email = user.getUsername();
            return userService.findByEmail(email);
        }

        private void uploadDir (MultipartFile[]files, String[]webkitRelativePaths,
                DiskUser user, DiskFolder parentFolder){
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

        private void uploadFile (MultipartFile file, DiskUser user, DiskFolder parentFolder){
            String fileName = file.getOriginalFilename();

            if (fileName == null) {
                return;
            }

            if (fileName.lastIndexOf('/') != -1) {
                fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
            }
            DiskFile dFile = fileService.createFile(user, fileName, file.getSize(), parentFolder);
            if (!"".equals(file.getOriginalFilename())) {
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


    }
