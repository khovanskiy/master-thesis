package com.khovanskiy.service;

import com.khovanskiy.model.Ref;
import com.khovanskiy.model.TrainRun;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * @author victor
 */
public class RepositoryTest {
    private Repository repository = new Repository();

    @Before
    public void save() {
        Ref<TrainRun> trainRunId1 = new TrainRun.Id("1");
        TrainRun trainRun1 = new TrainRun(trainRunId1, "T1", new ArrayList<>());
        repository.create(trainRun1);

        Ref<TrainRun> trainRunId2 = new TrainRun.Id("2");
        TrainRun trainRun2 = new TrainRun(trainRunId2, "T2", new ArrayList<>());
        repository.create(trainRun2);
    }

    @Test
    public void find() {
        Assert.assertTrue(repository.find(new TrainRun.Id("1")).isPresent());
    }

    @Test
    public void findAll() {
        Assert.assertEquals(2, repository.findAll(TrainRun.class).size());
    }

    @After
    public void after() {
        repository.clear();
    }
}
