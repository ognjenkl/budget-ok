import { Button, Form, Input, InputNumber } from 'antd';
import type { Envelope } from '../../api/getEnvelopes';
import useUpdateEnvelope from '../../hooks/useUpdateEnvelope';

type EditEnvelopeFormProps = {
  envelope: Envelope;
  onSuccess?: () => void;
  onCancel: () => void;
};

type EditEnvelopeFormData = {
  name: string;
  budget: number;
};

export default function EditEnvelopeForm({ envelope, onSuccess, onCancel }: EditEnvelopeFormProps) {
  const [form] = Form.useForm<EditEnvelopeFormData>();
  const { mutate, isPending } = useUpdateEnvelope(envelope.id);

  const handleSubmit = (values: EditEnvelopeFormData) => {
    mutate(values, {
      onSuccess: () => {
        onSuccess?.();
        onCancel();
      },
    });
  };

  return (
    <Form
      form={form}
      layout="vertical"
      onFinish={handleSubmit}
      initialValues={{
        name: envelope.name,
        budget: envelope.budget,
      }}
    >
      <Form.Item
        name="name"
        label="Envelope Name"
        rules={[{ required: true, message: 'Please input the envelope name' }]}
      >
        <Input placeholder="Envelope Name" />
      </Form.Item>
      <Form.Item
        name="budget"
        label="Budget"
        rules={[{ required: true, message: 'Please input the budget amount' }]}
      >
        <InputNumber style={{ width: '100%' }} />
      </Form.Item>
      <Form.Item style={{ textAlign: 'right' }}>
        <Button onClick={onCancel} style={{ marginRight: 8 }}>
          Cancel
        </Button>
        <Button type="primary" htmlType="submit" loading={isPending}>
          Save Changes
        </Button>
      </Form.Item>
    </Form>
  );
}
