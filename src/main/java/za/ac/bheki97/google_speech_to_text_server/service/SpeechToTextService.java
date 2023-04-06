package za.ac.bheki97.google_speech_to_text_server.service;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.speech.v1.*;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.*;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class SpeechToTextService {

    public String transcribeAudio( MultipartFile file,String audioLang,String transLang) throws IOException {
        SpeechClient speechClient = SpeechClient.create(SpeechSettings.newBuilder()
                        .setCredentialsProvider(
                                FixedCredentialsProvider.create(
                                        GoogleCredentials.fromStream(
                                                getClass()
                                                        .getClassLoader()
                                                        .getResourceAsStream("google_credentials.json")
                                        )
                                )
                        )
                .build());

            byte[] data = file.getBytes();
            ByteString audioBytes = ByteString.copyFrom(data);

            // Configure request with local raw PCM audio
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.FLAC)
                    .setAudioChannelCount(2)
                    .setEnableSpokenPunctuation(BoolValue.of(true))
                    .setSampleRateHertz(44100)
                    .setLanguageCode(audioLang)
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
                return alternative.getTranscript()
                        +"\n"+translateText(alternative.getTranscript(),audioLang,transLang);
                //"\n"+translateToEnglish(alternative.getTranscript())
            }

        return "No Speech";
    }
    
    public String translateText(String text,String audioLang,String transLang) throws IOException {
        Translate translate = TranslateOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(
                                getClass().getClassLoader().getResourceAsStream("google_credentials.json")))
                .build().getService();

        Translation translation = translate.translate(text, Translate.TranslateOption.sourceLanguage(audioLang),
                Translate.TranslateOption.targetLanguage(transLang));
        return translation.getTranslatedText();
    }

    public MultipartFile readtext(String text,String language) throws IOException {

        TextToSpeechClient textToSpeechClient = TextToSpeechClient.create(
                TextToSpeechSettings.newBuilder()
                        .setCredentialsProvider(
                                FixedCredentialsProvider.create(
                                        GoogleCredentials.fromStream(
                                                getClass()
                                                        .getClassLoader()
                                                        .getResourceAsStream("google_credentials.json")
                                        )
                                )
                        )
                        .build() );

            SynthesisInput input = SynthesisInput.newBuilder()
                    .setText(text)
                    .build();

            VoiceSelectionParams voice =
                    VoiceSelectionParams.newBuilder()
                            .setLanguageCode(language)
                            .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                            .build();

            AudioConfig audioConfig =
                    AudioConfig.newBuilder()
                            .setAudioEncoding(AudioEncoding.LINEAR16)
                            .build();

            SynthesizeSpeechResponse response =
                    textToSpeechClient.synthesizeSpeech(
                            SynthesizeSpeechRequest.newBuilder()
                                    .setInput(input)
                                    .setVoice(voice)
                                    .setAudioConfig(audioConfig)
                                    .build());

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(response.getAudioContent().toByteArray());

            return new MockMultipartFile("file", "audio.flac", "audio/flac", outputStream.toByteArray());



    }

}
