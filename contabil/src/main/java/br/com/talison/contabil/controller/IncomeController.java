package br.com.talison.contabil.controller;


import br.com.talison.contabil.domain.dto.ActivityDto;
import br.com.talison.contabil.domain.dto.IncomeDto;
import br.com.talison.contabil.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/income")
@RequiredArgsConstructor
public class IncomeController {

    private final IncomeService incomeService;

    @GetMapping("/all/{id}")
    public ResponseEntity<List<ActivityDto>> getAllIncomes(@PathVariable String id) {
        return ResponseEntity.ok(incomeService.listActivities(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncomeDto> getIncomeById(@PathVariable String id) {
        IncomeDto dto = incomeService.getIncomeById(id);

        if(dto != null) {
            return ResponseEntity.status(200).body(dto);
        }
        else {
            return ResponseEntity.status(204).build();
        }
    }

    @PostMapping
    public ResponseEntity<String> addIncome(@Valid @RequestBody IncomeDto income) {
        IncomeDto dto = incomeService.addIncome(income);

        if(dto != null){
            return ResponseEntity.status(201).body(dto.getId());
        }
        else{
            return ResponseEntity.status(409).body(null);
        }
    }

    @PutMapping
    public ResponseEntity<IncomeDto> updateIncome(@Valid @RequestBody IncomeDto dto) {
        IncomeDto ret = incomeService.updateIncome(dto);

        if(ret != null) {
            return ResponseEntity.status(200).body(ret);
        }
        else {
            return ResponseEntity.status(204).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncome(@PathVariable String id) {
        incomeService.delete(id);
        return ResponseEntity.status(200).build();

    }

    @PostMapping("/delete/bucket")
    public ResponseEntity<Void> deleteIncomeBucket(@RequestBody List<String> ids) {
        incomeService.deleteBucket(ids);
        return ResponseEntity.status(200).build();

    }

}
