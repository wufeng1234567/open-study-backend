package com.openstudy.notes.service.impl;

import com.openstudy.common.utils.DateUtils;
import com.openstudy.notes.domain.NoteQuestionBank;
import com.openstudy.notes.mapper.NoteQuestionBankMapper;
import com.openstudy.notes.service.INoteQuestionBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteQuestionBankServiceImpl implements INoteQuestionBankService {
    @Autowired
    private NoteQuestionBankMapper noteQuestionBankMapper;

    @Override
    public NoteQuestionBank selectNoteQuestionBankById(Long id) {
        return noteQuestionBankMapper.selectNoteQuestionBankById(id);
    }

    @Override
    public List<NoteQuestionBank> selectNoteQuestionBankList(NoteQuestionBank noteQuestionBank) {
        return noteQuestionBankMapper.selectNoteQuestionBankList(noteQuestionBank);
    }

    @Override
    public int insertNoteQuestionBank(NoteQuestionBank noteQuestionBank) {
        noteQuestionBank.setCreateTime(DateUtils.getNowDate());
        return noteQuestionBankMapper.insertNoteQuestionBank(noteQuestionBank);
    }

    @Override
    public int updateNoteQuestionBank(NoteQuestionBank noteQuestionBank) {
        return noteQuestionBankMapper.updateNoteQuestionBank(noteQuestionBank);
    }

    @Override
    public int deleteNoteQuestionBankById(Long id) {
        return noteQuestionBankMapper.deleteNoteQuestionBankById(id);
    }

    @Override
    public int deleteNoteQuestionBankByIds(Long[] ids) {
        return noteQuestionBankMapper.deleteNoteQuestionBankByIds(ids);
    }

    @Override
    public List<NoteQuestionBank> selectByNoteId(Long noteId) {
        return noteQuestionBankMapper.selectByNoteId(noteId);
    }

    @Override
    public List<NoteQuestionBank> selectByBankId(Long bankId) {
        return noteQuestionBankMapper.selectByBankId(bankId);
    }

    @Override
    public int deleteByNoteIdAndBankId(Long noteId, Long bankId) {
        return noteQuestionBankMapper.deleteByNoteIdAndBankId(noteId, bankId);
    }

    @Override
    public int countByNoteId(Long noteId) {
        return noteQuestionBankMapper.countByNoteId(noteId);
    }
}
