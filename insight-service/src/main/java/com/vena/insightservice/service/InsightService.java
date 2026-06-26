package com.vena.insightservice.service;

import com.vena.insightservice.client.UsageClient;
import com.vena.insightservice.dto.DeviceDto;
import com.vena.insightservice.dto.InsightDto;
import com.vena.insightservice.dto.UsageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InsightService {

    private final UsageClient usageClient;
    private final OllamaChatModel ollamaChatModel;

    public InsightDto getSavingsTips(Long userId) {
        final UsageDto usageData  = usageClient.getXDaysUsageForUser(userId, 3);

        Double totalUsage = usageData.devices().stream()
                .mapToDouble(DeviceDto::energyConsumed)
                .sum();
        log.info("Calling Ollama For userId : {} with total usage {}", userId, totalUsage);

        String prompt = String.format("""
                Bu veri, benim son 3 gün içindeki toplam enerji tüketimimdir. 
                Enerji tüketimimi nasıl azaltabilirim? Ortalama bir haneye göre durumum nasıl?
                Lütfen yanıtını tamamen TÜRKÇE olarak, Markdown (kalın harf vb.) kullanmadan düz metin olarak ver.
                Maddeler halinde kısa ve anlaşılır tasarruf ipuçları sun.
                
                Toplam Enerji Kullanımı: %.2f kWh
                """, totalUsage);

        ChatResponse response = ollamaChatModel.call(Prompt.builder()
                .content(prompt)
                .build()
        );
        return InsightDto.builder()
                .userId(userId)
                .tips(response.getResult().getOutput().getText())
                .energyUsage(totalUsage)
                .build();


    }

    public InsightDto getOverview(Long userId){
        final UsageDto usageData  = usageClient.getXDaysUsageForUser(userId, 3);

        Double totalUsage = usageData.devices().stream()
                .mapToDouble(DeviceDto::energyConsumed)
                .sum();
        log.info("Calling Ollama For userId : {} with total usage {}", userId, totalUsage);

        String prompt = String.format("""
                Aşağıdaki enerji tüketim verilerini analiz et ve son kullanıcıya yönelik eyleme geçirilebilir, kısa ve öz tavsiyeler sun.
                Lütfen yanıtını tamamen TÜRKÇE olarak ver. Markdown formatı (**kalın** vb.) KULLANMA, temiz ve okunaklı bir düz metin veya düz liste kullan.
                Doğrudan kullanıcıya hitap et (Örn: Şofbeniniz çok elektrik tüketiyor, ayarını kısmayı düşünebilirsiniz).
                
                Son 3 Günlük Kullanım Verisi:
                %s
                """, usageData.devices());        ChatResponse response = ollamaChatModel.call(Prompt.builder()
                .content(prompt)
                .build()
        );
        return InsightDto.builder()
                .userId(userId)
                .tips(response.getResult().getOutput().getText())
                .energyUsage(totalUsage)
                .build();

    }

}
