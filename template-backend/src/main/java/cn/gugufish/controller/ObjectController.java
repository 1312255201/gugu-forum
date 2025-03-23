package cn.gugufish.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class ObjectController {
    @GetMapping("/images/avatar/**")
    public void imageFetch(HttpServletRequest request, HttpServletResponse response) throws IOException {

    }
    private void fetchImage(HttpServletRequest request, HttpServletResponse response){
        String imagePath = request.getServletPath().substring(7);
    }
}
