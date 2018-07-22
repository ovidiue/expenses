package helpers.repositories;

import model.Expense;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Hibernate;
import org.hibernate.Session;

import java.util.List;

/**
 * Created by Ovidiu on 19-May-18.
 */
public class ExpenseRepository implements HibernateHlp<Expense> {

    @Override
    public int save(Expense expense) {
        Session session = sessionFactory.openSession();

        session.beginTransaction();

        int id = (int) session.save(expense);

        session.getTransaction().commit();

        session.close();

        return id;
    }

    @Override
    public void delete(Expense expense) {
        Session session = sessionFactory.openSession();

        session.beginTransaction();

        session.delete(expense);

        session.getTransaction().commit();

        session.close();
    }

    @Override
    public void update(Expense expense) {
        Session session = sessionFactory.openSession();

        session.beginTransaction();

        session.update(expense);

        session.getTransaction().commit();

        session.close();

    }

    @Override
    public Expense findById(int id) {
        Session session = sessionFactory.openSession();

        Expense expense = session.get(Expense.class, id);
        Hibernate.initialize(expense.getPayedRates());

        session.close();

        return expense;
    }

    @Override
    public List<Expense> fetchAll() {
        Session session = sessionFactory.openSession();

        Criteria criteria = session.createCriteria(Expense.class);

        List<Expense> expenses = criteria.list();

        session.close();

        return expenses;
    }

    public List<Expense> fetchAllWithRates() {
        Session session = sessionFactory.openSession();
        List<Expense> expenses = session.createCriteria(Expense.class)
                .setFetchMode("rate", FetchMode.EAGER)
                .list();

        return expenses;
    }


}
