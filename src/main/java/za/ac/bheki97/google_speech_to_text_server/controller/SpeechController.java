package za.ac.bheki97.google_speech_to_text_server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import za.ac.bheki97.google_speech_to_text_server.service.SpeechToTextService;


import java.io.IOException;

@RestController
@RequestMapping("/speech")
public class SpeechController {

    @Autowired
    private SpeechToTextService speechService;

    @PostMapping("/transcribe")
    public String transcribeAudio(@RequestParam("audio") MultipartFile audio,
                                  @RequestParam("language") String language) throws IOException {


        return speechService.transcribeAudio(audio,language);
    }

    @PostMapping("/read")
    public ResponseEntity<InputStreamResource> readText(@RequestParam("text") String text,
                                                        @RequestParam("language") String language) throws IOException {
        MultipartFile file =  speechService.readtext(text, language);
        InputStreamResource resource = new InputStreamResource(file.getInputStream());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE);

        //return speechService.readtext(text, language);
        return   ResponseEntity.ok()
                .header(String.valueOf(headers))
                .body(resource);
    }
}
