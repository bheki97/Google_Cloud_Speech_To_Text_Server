package za.ac.bheki97.google_speech_to_text_server.controller;

import jakarta.servlet.ServletOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.process.ProcessLocator;
import za.ac.bheki97.google_speech_to_text_server.model.dto.TranslateDto;
import za.ac.bheki97.google_speech_to_text_server.service.SpeechToTextService;


import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;

@RestController
@RequestMapping("/speech")
public class SpeechController {

    @Autowired
    private SpeechToTextService speechService;

    @PostMapping("/transcribe")
    public String transcribeAudio(@RequestParam("audio") MultipartFile audio,
                                  @RequestParam("language") String language) throws IOException, InterruptedException {
            File inputFile = new File("C:\\User\\VM JELE\\audios\\"+audio.getOriginalFilename()),
            outputFile = new File("C:\\User\\VM JELE\\audios\\prita-" +new SimpleDateFormat("yyyyMMdd_HHmmss",
                    Locale.ENGLISH).format(new Date())+".flac" );

            if(inputFile.exists()){
                inputFile.mkdir();
            }


        System.out.println("Transcribing......"+language);
        System.out.println("Path and Name: "+audio.getOriginalFilename());

        if(!inputFile.exists()){

            inputFile.mkdirs();

        }



        audio.transferTo(inputFile);
        ProcessBuilder builder = new ProcessBuilder("ffmpeg","-i",
            inputFile.getAbsolutePath(),"-ar","44100","-ac","2","-f","flac",outputFile.getAbsolutePath());
        Process process = builder.start();

        int exitCode = -1;

        exitCode = process.waitFor();

        if (exitCode == 0) {

            byte[] data = Files.readAllBytes(Path.of(outputFile.getAbsolutePath()));
            outputFile.delete();
            inputFile.delete();
            return speechService.transcribeAudio(data,language.replaceAll("\"",""));
        } else {

            return "Conversion failed.";
        }

    }

    @PostMapping("/audio-file")
    public String transcribeAudio(@RequestParam("audio") MultipartFile audio) throws IOException {
            File inputFile = new File("C:\\Users\\VM JELE\\audios\\"+audio.getOriginalFilename()),
                outputFile = new File("C:\\Users\\VM JELE\\audios\\prita-"+
                        new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date())+".flac" );


            if(!inputFile.exists()){
                inputFile.mkdir();
            }

            audio.transferTo(inputFile);
            ProcessBuilder builder = new ProcessBuilder("ffmpeg","-i",
                    inputFile.getAbsolutePath(),"-ar","44100","-ac","2","-f","flac",outputFile.getAbsolutePath());
        Process process = builder.start();
        int exitCode = -1;

        try {
                exitCode = process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (exitCode == 0) {
            byte[] data = Files.readAllBytes(Path.of(outputFile.getAbsolutePath()));

            return speechService.transcribeAudio(data,"en-US");
        } else {
            return "Conversion failed.";
        }
    }


    @PostMapping("/translate") public String translate(@RequestBody TranslateDto dto) throws IOException {

        String translation = speechService.translateText(dto.getText(), dto.getOriginLang(), dto.getTransLang());

        return translation;
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
