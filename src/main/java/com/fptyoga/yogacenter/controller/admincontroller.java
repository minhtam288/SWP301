package com.fptyoga.yogacenter.controller;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fptyoga.yogacenter.Entity.Booking;
import com.fptyoga.yogacenter.Entity.Content;
import com.fptyoga.yogacenter.Entity.Role;
import com.fptyoga.yogacenter.Entity.Trainer;
import com.fptyoga.yogacenter.Entity.User;
import com.fptyoga.yogacenter.dto.MonthlyTotal;
import com.fptyoga.yogacenter.repository.BookingRepository;
import com.fptyoga.yogacenter.repository.CourseRepository;
import com.fptyoga.yogacenter.repository.TrainerRepository;
import com.fptyoga.yogacenter.repository.UserRepository;
import com.fptyoga.yogacenter.service.BookingService;
import com.fptyoga.yogacenter.service.ClassesService;
import com.fptyoga.yogacenter.service.ContentService;
import com.fptyoga.yogacenter.service.CourseService;
import com.fptyoga.yogacenter.service.RoleService;
import com.fptyoga.yogacenter.service.TrainerService;
import com.fptyoga.yogacenter.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@PreAuthorize(value = "")
@RequestMapping("/admin")
public class admincontroller {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private ContentService contentService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ClassesService classesService;
    @Autowired
    private TrainerService trainerService;

    @GetMapping("/index")
    public String trainer(Model model, @RequestParam(defaultValue = "") Long roleid,HttpSession session) {
        User user = (User) session.getAttribute("user");
        if(user.getRole().getRoleid() != 1) return "redirect:/404";
        int countClass = classesService.totalClasses();
        long CountCustomer = userService.countUsersByRoleAndStatus();
        long SumAmout = bookingService.TotalAmount();
        int countCourse = courseService.totalCourse();
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        String formattedNumber = numberFormat.format(SumAmout);
        List<MonthlyTotal> monthlyTotalsUser = userService.getMonthlyUser();
        List<MonthlyTotal> monthlyTotals = bookingService.getMonthlyBookingAmount();

        model.addAttribute("countCourse", countCourse);
        model.addAttribute("countClass", countClass);
        model.addAttribute("CountCustomer", CountCustomer);
        model.addAttribute("formattedNumber", formattedNumber);
        model.addAttribute("monthlyTotalsUser", monthlyTotalsUser);
        model.addAttribute("monthlyTotals", monthlyTotals);
        if (roleid != null) {
            List<User> userList = userService.listAll(roleid);
            model.addAttribute("userList", userList);
        } else {
            List<User> userList = userService.getUserByStatus();
            model.addAttribute("userList", userList);
        }
        return "admin/index";
    }

    @GetMapping("/charts")
    public String chart(Model model) {
        List<MonthlyTotal> monthlyTotalsUser = userService.getMonthlyUser();
        model.addAttribute("monthlyTotalsUser", monthlyTotalsUser);

        List<MonthlyTotal> monthlyTotals = bookingService.getMonthlyBookingAmount();
        model.addAttribute("monthlyTotals", monthlyTotals);
        return "admin/charts";
    }

    @GetMapping("/accountDeleted")
    public String accountDeleted(Model model, @RequestParam(defaultValue = "") Long roleid) {

        int countClass = classesService.totalClasses();
        long CountCustomer = userService.countUsersByRoleAndStatus();
        long SumAmout = bookingService.TotalAmount();
        int countCourse = courseService.totalCourse();
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        String formattedNumber = numberFormat.format(SumAmout);
        List<MonthlyTotal> monthlyTotalsUser = userService.getMonthlyUser();
        List<MonthlyTotal> monthlyTotals = bookingService.getMonthlyBookingAmount();

        model.addAttribute("countCourse", countCourse);
        model.addAttribute("countClass", countClass);
        model.addAttribute("CountCustomer", CountCustomer);
        model.addAttribute("formattedNumber", formattedNumber);
        model.addAttribute("monthlyTotalsUser", monthlyTotalsUser);
        model.addAttribute("monthlyTotals", monthlyTotals);
        if (roleid != null) {
            List<User> userList = userService.listAllUserFalse(roleid);
            model.addAttribute("userList", userList);
        } else {
            List<User> userList = userService.getUserByStatusFalse();
            model.addAttribute("userList", userList);
        }
        return "admin/accountDeleted";
    }

    @GetMapping("/updatedTrue/{id}")
    public String updatedTrue(@PathVariable("id") Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        user.setStatus(true);
        userRepository.save(user);
        return "redirect:/admin/accountDeleted";
    }

    @GetMapping("/adduser")
    public String addUser(Model model, @RequestParam(value = "id", required = false) Long id) {
        User user;
        List<Role> rolesList = roleService.allRole();
        model.addAttribute("rolesList", rolesList);
        if (id == null || id == 0) {
            user = new User();
            model.addAttribute("id", 0); // Add this line to explicitly set the ID as 0
        } else {
            user = userRepository.findById(id).orElse(null);
            model.addAttribute("id", id);
        }
        model.addAttribute("user", user);
        model.addAttribute("trainer", new Trainer());
        return "admin/adduser";
    }

    @PostMapping("/adduser/new")
    public String newUser(@ModelAttribute User user,
            @RequestParam(value = "id", required = false) Long id,
            RedirectAttributes ra,
            @RequestParam("file") MultipartFile file,
            Model model) {
        

        if (id == null || id == 0) {
            if (userRepository.existsByEmail(user.getEmail())) {
                ra.addFlashAttribute("existemail", "The Email already exists.");
                return "redirect:/admin/adduser";
            }
            user.setRegistrationdate(LocalDate.now());
            user.setStatus(true);
            try {
                userService.saveUser(file, user);
            } catch (IOException e) {
                // Xử lý lỗi nếu cần
            }
            userRepository.save(user);

            return "redirect:/admin/index";
        } else {
            User chkUser = userRepository.findById(id).orElse(null);
            if (!chkUser.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(user.getEmail())) {
                ra.addFlashAttribute("existemail", "The Email already exists.");
                return "redirect:/admin/adduser";
            }
            user.setRegistrationdate(chkUser.getRegistrationdate());
            user.setStatus(true);

        }

        try {
            userService.saveUser(file, user);
        } catch (IOException e) {
            // Xử lý lỗi nếu cần
        }
        userRepository.save(user);

        ra.addFlashAttribute("message", "The user has been saved successfully.");
        return "redirect:/admin/edit/" + id;
    }

    @GetMapping("/edit-trainer")
    public String editTrainer(Model model, @RequestParam(value = "id") Long id) {
        Trainer trainer = trainerRepository.findById(id).orElse(null);
        model.addAttribute("id", id);
        model.addAttribute("trainer", trainer);
        return "admin/edit-trainer";
    }

    @PostMapping("/updateTrainer")
    public String updateTrainer(@RequestParam(value = "id") Long id, Model model,
            @ModelAttribute Trainer trainers) {
        trainerRepository.save(trainers);
        return "redirect:/admin/index";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        user.setStatus(false);
        userRepository.save(user);
        return "redirect:/admin/index";
    }

    @GetMapping("/edit/{id}")
    public String showEdit(@PathVariable("id") Long id, Model model) {
        List<Role> rolesList = roleService.allRole();
        model.addAttribute("rolesList", rolesList);
        try {
            User user = userRepository.findById(id).orElse(null);
            model.addAttribute("user", user);
            return "admin/adduser";
        } catch (Exception e) {
        }
        return "admin/adduser";
    }

    @GetMapping("/download-png")
    public ResponseEntity<Resource> downloadPng(@RequestParam(defaultValue = "") Long userid) {
        byte[] pngData = userService.getPngDataById(userid);
        if (pngData != null) {
            ByteArrayResource resource = new ByteArrayResource(pngData);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=image.png")
                    .contentType(MediaType.IMAGE_PNG)
                    .body(resource);
        }
        // Xử lý trường hợp tệp tin không tồn tại
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/upload")
    public String uploadFile(Model model) {
        model.addAttribute("content", new Content());
        return "admin/upload";
    }

    @PostMapping("/upload")
    public String uploadContent(@ModelAttribute("content") Content content,
            @RequestParam("file") MultipartFile file) {
        try {
            contentService.saveContent(file, content);
        } catch (IOException e) {
            // Xử lý lỗi nếu cần
        }
        return "redirect:/admin/upload";
    }

    @GetMapping("/image-content")
    public ResponseEntity<Resource> downloadContentPng(@RequestParam(defaultValue = "") Long contentId) {
        byte[] pngData = contentService.getPngDataById(contentId);
        if (pngData != null) {
            ByteArrayResource resource = new ByteArrayResource(pngData);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=image.png")
                    .contentType(MediaType.IMAGE_PNG)
                    .body(resource);
        }
        // Xử lý trường hợp tệp tin không tồn tại
        return ResponseEntity.notFound().build();
    }

    // @PostMapping("/document")
    // public String uploadDocument(Course course, Model model){
    // courseService.saveCourse(course);

    // model.addAttribute("message", "upload successful");
    // return "/admin/upload";
    // }

    @GetMapping("/booking")
    public String booking(Model model) {

        List<Booking> booking = bookingService.getRevenue();
        int countClass = classesService.totalClasses();
        long CountCustomer = userService.countUsersByRoleAndStatus();
        long SumAmout = bookingService.TotalAmount();
        int countCourse = courseService.totalCourse();
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        String formattedNumber = numberFormat.format(SumAmout);
        List<MonthlyTotal> monthlyTotalsUser = userService.getMonthlyUser();
        List<MonthlyTotal> monthlyTotals = bookingService.getMonthlyBookingAmount();

        model.addAttribute("countCourse", countCourse);
        model.addAttribute("countClass", countClass);
        model.addAttribute("CountCustomer", CountCustomer);
        model.addAttribute("formattedNumber", formattedNumber);
        model.addAttribute("monthlyTotalsUser", monthlyTotalsUser);
        model.addAttribute("monthlyTotals", monthlyTotals);

        model.addAttribute("booking", booking);
        return "admin/booking";
    }
}
