package com.gmail.dimabah.ddisk.controllers;

import com.gmail.dimabah.ddisk.models.DiskFile;
import com.gmail.dimabah.ddisk.models.DiskFolder;
import com.gmail.dimabah.ddisk.models.DiskUser;
import com.gmail.dimabah.ddisk.models.enums.UserRole;
import com.gmail.dimabah.ddisk.services.DiskFileService;
import com.gmail.dimabah.ddisk.services.DiskFolderService;
import com.gmail.dimabah.ddisk.services.DiskUserService;
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

@Controller
public class DiskController {
    private DiskUserService userService;
    private PasswordEncoder encoder;
    private DiskFolderService folderService;

    private DiskFileService fileService;

    public DiskController(DiskUserService userService, PasswordEncoder encoder, DiskFolderService folderService, DiskFileService fileService) {
        this.userService = userService;
        this.encoder = encoder;
        this.folderService = folderService;
        this.fileService = fileService;
    }

    @GetMapping("/")
    public String index(Model model) {
        User user = getCurrentUser();

        String email = user.getUsername();
        DiskUser dUser = userService.findByEmail(email);

        if (dUser == null) {
            return "unauthorized";
        }

//        model.addAttribute("email",email);
        model.addAttribute("folder", dUser.getMainFolder().toFolderDTO());

        return "index";
    }

    @GetMapping("folders/{address}")
    @PreAuthorize("hasPermission(#address, 'read')")
    public String folders(@PathVariable("address") String address, Model model) {
//        User user = getCurrentUser();
//
//        String email = user.getUsername();
//        DiskUser dUser = userService.findByEmail(email);
//
//        if (dUser == null) {
//            return "unauthorized";
//        }

        DiskFolder folder = folderService.findByAddress(address);
        if (folder != null) {
            model.addAttribute("folder", folder.toFolderDTO());
        }

        return "index";
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
    public String update(@RequestParam(required = false) String newFolder,
                         @RequestParam(required = false) String currentFolder,
                         @RequestParam(required = false) MultipartFile[] files ){

        User user = getCurrentUser();

        String email = user.getUsername();
        DiskUser dUser = userService.findByEmail(email);
        DiskFolder dFolder = folderService.findByAddress(currentFolder);

        if (dUser == null) {
            return "unauthorized";
        }
        if (newFolder != null) {
            DiskFolder newDiskFolder = folderService.createFolder(dUser, newFolder);
            folderService.addFolder(dFolder, newDiskFolder);
        }

        if (files != null) {
            for (var file:files) {
                DiskFile dFile = fileService.createFile(dUser, file.getOriginalFilename(), file.getSize());
                dFolder.addFile(dFile);
                folderService.updateFolder(dFolder);
                if (!"".equals(file.getOriginalFilename())) {
                    try {
                        String dirPath = "D:/upload_dir/"+ email + "/"+ dFile.getAddress() + "/";
                        File dir = new File(dirPath);
                        dir.mkdirs();
                        String fileName = file.getOriginalFilename();
                        file.transferTo(new File(dirPath + fileName));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return "redirect:/folders/" + currentFolder;
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

}
