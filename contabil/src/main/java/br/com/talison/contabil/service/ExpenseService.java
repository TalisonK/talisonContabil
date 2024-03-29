package br.com.talison.contabil.service;

import br.com.talison.contabil.domain.*;
import br.com.talison.contabil.domain.List;
import br.com.talison.contabil.domain.dto.*;
import br.com.talison.contabil.domain.enums.EnumPaymentMethod;
import br.com.talison.contabil.repository.CategoryRepository;
import br.com.talison.contabil.repository.ExpenseRepository;
import br.com.talison.contabil.repository.ListRepository;
import br.com.talison.contabil.repository.UserRepository;
import br.com.talison.contabil.service.mapper.ActivityExpenseMapper;
import br.com.talison.contabil.service.mapper.ExpenseMapper;
import br.com.talison.contabil.service.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ListRepository listRepository;

    private final ExpenseMapper expenseMapper;
    private final ActivityExpenseMapper activityExpenseMapper;

    private final TotalsService totalsService;


    private final DateUtils dateUtils = new DateUtils();


    private Expense sendExpense(ExpenseDto expense, Category category, User user) {
        Expense novo = new Expense(
                expense.getDescription(),
                expense.getPaymentMethod(),
                category,
                expense.getValue(),
                user,
                expense.getPaidAt(),
                expense.getActualParcel(),
                expense.getTotalParcel());
        return expenseRepository.save(novo);
    }

    public java.util.List<ActivityDto> listActivities(String id) {

        Optional<java.util.List<Expense>> expenses = expenseRepository.findAllByUserId(id);

        if(expenses.isEmpty()) {
            return Collections.emptyList();
        }

        java.util.List<ActivityDto> data = activityExpenseMapper.toDto(expenses.get());

        data = data.stream().map((dto) -> {
            dto.setType("Expense");
            return dto;
        }).toList();

        return data;
    }

    public java.util.List<String> addExpense(ExpenseDto expense) {

        //validations
        if(expense.getPaymentMethod() == EnumPaymentMethod.CREDIT_CARD && (expense.getTotalParcel() == null || expense.getActualParcel() == null)) {
            return null;
        }

        if((expense.getTotalParcel() != null && expense.getActualParcel() != null) && (expense.getTotalParcel() < expense.getActualParcel())) {
            return null;
        }

        Optional<Category> category = categoryRepository.findByName(expense.getCategory());
        Optional<User> user = userRepository.findByName(expense.getUser());

        if(category.isEmpty() || user.isEmpty()) {
            return null;
        }

        if(expense.getPaymentMethod() == EnumPaymentMethod.CREDIT_CARD) {

            int year = expense.getPaidAt().getYear();
            int month = expense.getPaidAt().getMonthValue();

            if(expense.getPaidAt().getDayOfMonth() <= 10){
                expense.setPaidAt(LocalDateTime.of(year, month, 15, 0, 0, 0));
            } else {
                if(month == 12) {
                    year++;
                    month = 1;
                } else {
                    month++;
                }
                expense.setPaidAt(LocalDateTime.of(year, month, 15, 0, 0, 0));
            }

            java.util.List<String> results = new ArrayList<>();

            for(; expense.getActualParcel() <= expense.getTotalParcel(); expense.setActualParcel(expense.getActualParcel() + 1)) {
                results.add(sendExpense(expense, category.get(), user.get()).getId());
                totalsService.updateTotals(expense.getPaidAt(), user.get().getId(), "expense");
                if(month == 12) {
                    year++;
                    month = 1;
                } else {
                    month++;
                }
                expense.setPaidAt(LocalDateTime.of(year, month, 15, 0, 0, 0));
            }

            return results;

        } else {
            expense.setActualParcel(null);
            expense.setTotalParcel(null);
            java.util.List<String> results = java.util.List.of(sendExpense(expense, category.get(), user.get()).getId());

            totalsService.updateTotals(expense.getPaidAt(), user.get().getId(), "expense");
            return results;
        }
    }

    public ExpenseDto updateExpense(ExpenseDto dto) {
        if (expenseRepository.existsById(dto.getId())) {

            Optional<User> user = userRepository.findByName(dto.getUser());

            expenseRepository.save(expenseMapper.toEntity(dto));
            totalsService.updateTotals(dto.getPaidAt(), user.get().getId(), "expense");
            return dto;
        }
        return null;
    }

    public void delete(String id) {
        expenseRepository.deleteById(id);
    }


    public ExpenseDto getExpenseById(String id) {
        return expenseMapper.toDto(expenseRepository.findById(id).orElse(null));
    }

    public java.util.List<Expense> listAllByMonth(String userId, Date start, Date end) {

        Optional<java.util.List<Expense>> incomes = expenseRepository.findAllByUserIdAndPaidAtBetweenOrderByPaidAt(userId, start, end);

        return incomes.orElse(null);

    }

    public void deleteBucket(java.util.List<String> ids) {
        for(String id : ids) {
            Expense expense = expenseRepository.findById(id).orElse(null);
            if(expense != null) {
                expenseRepository.deleteById(id);
                totalsService.updateTotals(expense.getPaidAt(), expense.getUser().getId(), "expense");
            }
        }
    }

    public Boolean addExpenseWithList(ExpenseListDto dto) {

        ExpenseDto expense = dto;
        java.util.List<ListItemDto> list = dto.getList();

        addExpense(expense);

        List listDto = new List();
        listDto.setList(list);
        listDto.setUser(expense.getUser());
        listDto.setCreatedAt(LocalDateTime.now());
        listDto.setDescription(expense.getDescription());

        listRepository.save(listDto);

        return true;
    }
}
