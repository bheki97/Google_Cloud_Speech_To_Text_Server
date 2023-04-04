package za.ac.bheki97.google_speech_to_text_server.service;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.cloud.speech.v1.*;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class SpeechToTextService {

    public String transcribeAudio( MultipartFile file) throws IOException {
        SpeechClient speechClient = SpeechClient.create(SpeechSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(
                        GoogleCredentials.fromStream(
                                getClass().getClassLoader().getResourceAsStream("google_credentials.json")
                        )
                ))
                .build());

            byte[] data = file.getBytes();
            ByteString audioBytes = ByteString.copyFrom(data);

            // Configure request with local raw PCM audio
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.FLAC)
                    .setEnableSpokenPunctuation(BoolValue.of(true))
                    .setSampleRateHertz(44100)
                    .setLanguageCode("en-US")
                    .build();
            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(audioBytes)
                    .build();

            // Use blocking call to get transcription results
            RecognizeResponse response = speechClient.recognize(config, audio);
            for (SpeechRecognitionResult result : response.getResultsList()) {
                // There can be several alternative transcripts for a given chunk of speech.
                // Just use the first (most likely) one here.
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                return alternative.getTranscript();
            }

        return "No Speech";
    }

}
