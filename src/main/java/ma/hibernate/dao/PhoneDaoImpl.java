package ma.hibernate.dao;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ma.hibernate.model.Phone;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class PhoneDaoImpl extends AbstractDao implements PhoneDao {
    public PhoneDaoImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    public Phone create(Phone phone) {
        Transaction transaction = null;
        try (Session session = super.factory.openSession()) {
            transaction = session.beginTransaction();
            session.save(phone);
            transaction.commit();
            return phone;
        } catch (Exception e) {
            assert transaction != null;
            transaction.rollback();
            throw new RuntimeException("Cannot save phone " + phone, e);
        }
    }

    @Override
    public List<Phone> findAll(Map<String, String[]> params) {
        Session session = super.factory.openSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Phone> cq = cb.createQuery(Phone.class);
        Root<Phone> root = cq.from(Phone.class);

        List<Predicate> predicates = new ArrayList<>();

        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            String fieldName = entry.getKey();
            String[] values = entry.getValue();

            predicates.add(values.length == 1
                    ? cb.equal(root.get(fieldName), values[0])
                    : root.get(fieldName).in((Object[]) values));
        }

        cq.select(root).where(cb.and(predicates.toArray(new Predicate[0])));

        return session.createQuery(cq).getResultList();
    }

}
