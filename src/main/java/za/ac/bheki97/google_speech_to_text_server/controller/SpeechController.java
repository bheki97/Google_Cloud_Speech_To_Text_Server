package za.ac.bheki97.google_speech_to_text_server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import za.ac.bheki97.google_speech_to_text_server.service.SpeechToTextService;

import java.io.IOException;

@RestController
@RequestMapping("/speech")
public class SpeechController {

    @Autowired
    private SpeechToTextService speechService;

    @PostMapping("/transcribe")
    public String transcribeAudio(@RequestParam("file") MultipartFile file) throws IOException {


        return speechService.transcribeAudio(file);
    }
}
